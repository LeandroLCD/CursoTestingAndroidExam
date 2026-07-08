package com.aristidevs.cursotestingandroid.checkout.presentation

import com.aristidevs.cursotestingandroid.cart.domain.model.CartSummary
import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

sealed class CheckoutUiState {
    data object Loading : CheckoutUiState()
    data class Success(val confirmation: OrderConfirmation) : CheckoutUiState()
    data class Error(val message:String) : CheckoutUiState()
    data class Idle(
        val summary: CartSummary,
        val form: CheckoutForm,
        val errors: CheckoutFormErrors,
        val isCartEmpty: Boolean,
        val isSubmitting: Boolean,
        val canSubmit: Boolean
    ) : CheckoutUiState()
}
