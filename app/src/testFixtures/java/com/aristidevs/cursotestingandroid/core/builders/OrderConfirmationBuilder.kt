package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

class OrderConfirmationBuilder {
    private var orderId: String = "order-1"
    private var etaMinutes: Int = 30
    private var total: Double = 100.0

    fun withOrderId(orderId: String) = apply { this.orderId = orderId }

    fun withEtaMinutes(etaMinutes: Int) = apply { this.etaMinutes = etaMinutes }

    fun withTotal(total: Double) = apply { this.total = total }

    fun build() =
        OrderConfirmation(
            orderId = orderId,
            etaMinutes = etaMinutes,
            total = total,
        )
}

fun orderConfirmation(block: OrderConfirmationBuilder.() -> Unit = {}) = OrderConfirmationBuilder().apply(block).build()
