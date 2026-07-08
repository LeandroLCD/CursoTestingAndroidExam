package com.aristidevs.cursotestingandroid.checkout.data.mapper

import com.aristidevs.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

fun OrderConfirmationResponse.toDomain(): OrderConfirmation{
    return OrderConfirmation(
        orderId = orderId,
        etaMinutes = etaMinutes,
        total = total
    )
}
