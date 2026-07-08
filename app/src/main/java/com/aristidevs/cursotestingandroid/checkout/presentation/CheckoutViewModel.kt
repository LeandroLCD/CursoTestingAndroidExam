package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CheckoutViewModel
    @Inject
    constructor(
        private val placeOrderUseCase: PlaceOrderUseCase,
        getCartSummaryUseCase: GetCartSummaryUseCase,
    ) : ViewModel() {
        private val formState = MutableStateFlow(CheckoutForm())

        private val submission = MutableStateFlow<Submission>(Submission.Idle)

        private val _events = MutableSharedFlow<CheckoutEvent>(extraBufferCapacity = 1)
        val event: SharedFlow<CheckoutEvent> = _events

        val uiState: StateFlow<CheckoutUiState> = combine(
            getCartSummaryUseCase(),
            formState,
            submission,
        ) { summary, form, sub ->
            when (sub) {
                is Submission.Success -> CheckoutUiState.Success(sub.confirmation)
                is Submission.Failed -> CheckoutUiState.Error(sub.message)
                Submission.Idle, Submission.Submitting -> {
                    val errors = form.validate()
                    val isCartEmpty = summary.subtotal <= 0.0
                    val isSubmitting = sub == Submission.Submitting
                    CheckoutUiState.Idle(
                        summary = summary,
                        form = form,
                        errors = errors,
                        isCartEmpty = isCartEmpty,
                        isSubmitting = isSubmitting,
                        canSubmit = !isCartEmpty && !isSubmitting && errors.isValid,
                    )
                }
            }
        }.catch { e ->
            _events.emit(CheckoutEvent.ShowMessage(e.message.orEmpty()))
            emit(CheckoutUiState.Error(e.message.orEmpty()))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CheckoutUiState.Loading,
        )

        fun onRetry() {
            submission.value = Submission.Idle
        }

        fun onNameChange(name: String) {
            formState.update { it.copy(name = name) }
        }

        fun onAddressChange(address: String) {
            formState.update { it.copy(address = address) }
        }

        fun onEmailChange(email: String) {
            formState.update { it.copy(email = email) }
        }

        fun onConfirm() {
            if (!formState.value.validate().isValid) return

            viewModelScope.launch {
                submission.value = Submission.Submitting
                placeOrderUseCase()
                    .onSuccess {
                        submission.value = Submission.Success(it)
                    }.onFailure { e ->
                        submission.value = Submission.Failed(e.message.orEmpty())
                        _events.emit(CheckoutEvent.ShowMessage(e.message.orEmpty()))
                    }
            }
        }
    }
