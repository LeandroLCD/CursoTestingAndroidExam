package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import kotlinx.serialization.json.Json

class OrderConfirmationResponseBuilder {
    private var orderId: String = "test01"

    private var etaMinutes: Int = 10

    private var total: Double = 200.00

    fun withOrderId(orderId: String) = apply { this.orderId = orderId }

    fun withEtaMinutes(etaMinutes: Int) = apply { this.etaMinutes = etaMinutes }

    fun withTotal(total: Double) = apply { this.total = total }

    fun build() = OrderConfirmationResponse(
        orderId = orderId,
        etaMinutes = etaMinutes,
        total = total,
    )

    fun buildJson(): String {
        return Json.encodeToString(OrderConfirmationResponse.serializer(), build())
    }
}

fun orderConfirmationResponse(block: OrderConfirmationResponseBuilder.() -> Unit = {}) = OrderConfirmationResponseBuilder().apply(block).build()
