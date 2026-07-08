package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem

class CartItemBuilder {
    private var productId: String = "product-1"
    private var quantity: Int = 1

    fun withProductId(productId: String) = apply { this.productId = productId }

    fun withQuantity(quantity: Int) = apply { this.quantity = quantity }

    fun build() =
        CartItem(
            productId = productId,
            quantity = quantity,
        )
}

fun cartItem(block: CartItemBuilder.() -> Unit = {}) = CartItemBuilder().apply(block).build()
