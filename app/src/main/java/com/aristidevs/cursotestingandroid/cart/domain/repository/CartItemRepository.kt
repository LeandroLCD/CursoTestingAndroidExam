package com.aristidevs.cursotestingandroid.cart.domain.repository

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartItemRepository {
    fun getCartItems(): Flow<List<CartItem>>

    suspend fun addToCart(
        productId: String,
        quantity: Int,
    )

    suspend fun removeFromCart(productId: String)

    suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    )

    suspend fun clearCart()

    suspend fun getCartItemById(productId: String): CartItem?
}
