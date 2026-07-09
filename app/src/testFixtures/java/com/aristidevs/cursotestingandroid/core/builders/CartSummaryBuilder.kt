package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.cart.domain.model.CartSummary

class CartSummaryBuilder {
    private var subtotal: Double = 100.0
    private var discountTotal: Double = 10.0
    private var finalTotal: Double = 90.0

    fun withSubtotal(subtotal: Double) = apply { this.subtotal = subtotal }

    fun withDiscountTotal(discountTotal: Double) = apply { this.discountTotal = discountTotal }

    fun withFinalTotal(finalTotal: Double) = apply { this.finalTotal = finalTotal }

    fun build() =
        CartSummary(
            subtotal = subtotal,
            discountTotal = discountTotal,
            finalTotal = finalTotal,
        )
}

fun cartSummary(block: CartSummaryBuilder.() -> Unit = {}) = CartSummaryBuilder().apply(block).build()
