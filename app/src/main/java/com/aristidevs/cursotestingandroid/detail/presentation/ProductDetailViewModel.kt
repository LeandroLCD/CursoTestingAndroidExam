package com.aristidevs.cursotestingandroid.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aristidevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.domain.model.AppError.*
import com.aristidevs.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.aristidevs.cursotestingandroid.detail.presentation.ProductDetailEvent.SUCCESS_ADD_TO_CART
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel
    @Inject
    constructor(
        private val getProductDetailWithPromotionUseCase: GetProductDetailWithPromotionUseCase,
        private val addToCartUseCase: AddToCartUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState())
        val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<ProductDetailEvent> = _events

        private var productJob: Job? = null

        fun loadProduct(productId: String) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            productJob?.cancel()
            productJob =
                getProductDetailWithPromotionUseCase(productId)
                    .onEach { product ->
                        _uiState.value = _uiState.value.copy(isLoading = false, item = product)
                    }.catch { e: Throwable ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        if (e is AppError) {
                            handleError(e)
                        } else {
                            handleError(UnknownError(e.message))
                        }
                    }.launchIn(viewModelScope)
        }

        fun addToCart() {
            val product =
                _uiState.value.item
                    ?.product
                    ?.id ?: return
            viewModelScope.launch {
                try {
                    addToCartUseCase(product)
                    _events.emit(SUCCESS_ADD_TO_CART)
                } catch (e: AppError) {
                    handleError(e)
                } catch (e: Exception) {
                    handleError(UnknownError(e.message))
                }
            }
        }

        private suspend fun handleError(e: AppError) {
            val newEvent =
                when (e) {
                    NetworkError -> ProductDetailEvent.NETWORK_ERROR
                    is Validation.InsufficientStock -> ProductDetailEvent.INSUFFICIENT_STOCK_ERROR
                    is UnknownError, DatabaseError, NotFoundError, Validation.QuantityMustBePositive -> ProductDetailEvent.UNKNOWN_ERROR
                }
            _events.emit(newEvent)
        }
    }
