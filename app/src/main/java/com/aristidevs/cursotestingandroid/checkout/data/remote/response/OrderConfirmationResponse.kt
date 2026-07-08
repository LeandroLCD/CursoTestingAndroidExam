package com.aristidevs.cursotestingandroid.checkout.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OrderConfirmationResponse(
    @SerialName("orderId")val orderId: String,
    @SerialName("etaMinutes")val etaMinutes: Int,
    @SerialName("total")val total: Double,
)
