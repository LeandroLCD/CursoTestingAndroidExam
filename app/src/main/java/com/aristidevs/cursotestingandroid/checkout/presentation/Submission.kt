package com.aristidevs.cursotestingandroid.checkout.presentation

import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

sealed interface Submission {
    data object Idle : Submission

    data object Submitting : Submission

    data class Success(
        val confirmation: OrderConfirmation,
    ) : Submission

    data class Failed(
        val message: String,
    ) : Submission
}
