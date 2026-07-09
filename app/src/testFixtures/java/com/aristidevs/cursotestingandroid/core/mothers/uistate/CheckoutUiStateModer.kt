package com.aristidevs.cursotestingandroid.core.mothers.uistate

import com.aristidevs.cursotestingandroid.cart.domain.model.CartSummary
import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.aristidevs.cursotestingandroid.checkout.presentation.CheckoutForm
import com.aristidevs.cursotestingandroid.checkout.presentation.CheckoutFormErrors
import com.aristidevs.cursotestingandroid.checkout.presentation.CheckoutUiState
import com.aristidevs.cursotestingandroid.core.builders.cartSummary
import com.aristidevs.cursotestingandroid.core.builders.checkoutForm
import com.aristidevs.cursotestingandroid.core.builders.checkoutFormErrors
import com.aristidevs.cursotestingandroid.core.builders.orderConfirmation

object CheckoutUiStateModer {
    val orderConfirmationDefault = orderConfirmation { withOrderId("1234") }

    val cartSummaryDefault = cartSummary {
        withSubtotal(100.0)
        withDiscountTotal(10.0)
        withFinalTotal(90.0)
    }

    val checkoutFormDefault = checkoutForm {
        withName("John Doe")
        withAddress("123 Main St")
        withEmail("john_doe@mail.cl")
    }

    val checkoutFormErrorsDefault = checkoutFormErrors {
        withNameError(null)
        withAddressError(null)
        withEmailError(null)
    }

    fun checkoutSuccess(
        confirmation: OrderConfirmation = orderConfirmationDefault,
    ) = CheckoutUiState.Success(
        confirmation = confirmation,
    )

    fun checkoutIdle(
        summary: CartSummary = cartSummaryDefault,
        form: CheckoutForm = checkoutFormDefault,
        errors: CheckoutFormErrors = checkoutFormErrorsDefault,
        isCartEmpty: Boolean = false,
        isSubmitting: Boolean = false,
        canSubmit: Boolean = true,
    ) = CheckoutUiState.Idle(
        summary = summary,
        form = form,
        errors = errors,
        isCartEmpty = isCartEmpty,
        isSubmitting = isSubmitting,
        canSubmit = canSubmit,
    )

    fun checkoutError(
        message: String = "Test Error",
    ) = CheckoutUiState.Error(
        message = message,
    )
}
