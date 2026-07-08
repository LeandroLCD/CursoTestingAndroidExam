package com.aristidevs.cursotestingandroid.checkout.presentation

sealed interface CheckoutEvent {
    data class ShowMessage(
        val message: String,
    ) : CheckoutEvent
}
