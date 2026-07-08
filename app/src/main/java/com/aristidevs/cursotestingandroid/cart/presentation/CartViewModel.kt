package com.aristidevs.cursotestingandroid.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel
    @Inject
    constructor(
        private val cartItemRepository: CartItemRepository,
        getCartSummaryUseCase: GetCartSummaryUseCase,
        private val updateCartItemUseCase: UpdateCartItemUseCase,
        getCartItemsWithPromotionsUseCase: GetCartItemsWithPromotionsUseCase,
    ) : ViewModel() {
        private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        val uiState: StateFlow<CartUiState> =
            combine(
                refreshTrigger.onStart { emit(Unit) },
                getCartItemsWithPromotionsUseCase(),
                getCartSummaryUseCase(),
            ) { _, cartItemWithPromotion, summary ->
                CartUiState.Success(
                    summary = summary,
                    cartItems = cartItemWithPromotion,
                    isLoading = false,
                ) as CartUiState
            }.catch { e ->
                _events.emit(CartEvent.ShowMessage(e.message.orEmpty()))
                emit(CartUiState.Error(e.message.orEmpty()))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CartUiState.Loading,
            )

        private val _events = MutableSharedFlow<CartEvent>(extraBufferCapacity = 1)
        val event: SharedFlow<CartEvent> = _events

        fun updateCartItem(
            productId: String,
            quantity: Int,
        ) {
            viewModelScope.launch {
                try {
                    updateCartItemUseCase(productId, quantity)
                } catch (e: Exception) {
                    _events.emit(CartEvent.ShowMessage(e.message.orEmpty()))
                }
            }
        }

        fun removeFromCart(productId: String) {
            viewModelScope.launch {
                try {
                    cartItemRepository.removeFromCart(productId)
                } catch (e: Exception) {
                    _events.emit(CartEvent.ShowMessage(e.message.orEmpty()))
                }
            }
        }

        fun increaseQuantity(
            productId: String,
            currentQuantity: Int,
        ) {
            updateCartItem(productId, currentQuantity + 1)
        }

        fun decreaseQuantity(
            productId: String,
            currentQuantity: Int,
        ) {
            if (currentQuantity > 1) {
                updateCartItem(productId, currentQuantity - 1)
            } else {
                removeFromCart(productId)
            }
        }

        fun refresh() {
            refreshTrigger.tryEmit(Unit)
        }
    }
