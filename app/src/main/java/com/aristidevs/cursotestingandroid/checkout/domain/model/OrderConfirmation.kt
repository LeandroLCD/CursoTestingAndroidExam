package com.aristidevs.cursotestingandroid.checkout.domain.model

data class OrderConfirmation(
    val orderId: String,
    val etaMinutes: Int,
    val total: Double,
)
