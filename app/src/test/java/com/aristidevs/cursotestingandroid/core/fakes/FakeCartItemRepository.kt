package com.aristidevs.cursotestingandroid.core.fakes

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCartItemRepository : CartItemRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    fun setCartItems(items: List<CartItem>) {
        _cartItems.value = items
    }

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()

    override suspend fun addToCart(
        productId: String,
        quantity: Int,
    ) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            val item = currentItems[existingIndex]
            currentItems[existingIndex] = item.copy(quantity = item.quantity + quantity)
        } else {
            currentItems.add(CartItem(productId, quantity))
        }
        _cartItems.value = currentItems
    }

    override suspend fun removeFromCart(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            currentItems.removeAt(existingIndex)
            _cartItems.value = currentItems
        } else {
            throw AppError.NotFoundError
        }
    }

    override suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    ) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            currentItems[existingIndex] = currentItems[existingIndex].copy(quantity = quantity)
            _cartItems.value = currentItems
        } else {
            throw AppError.NotFoundError
        }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override suspend fun getCartItemById(productId: String): CartItem? = _cartItems.value.find { it.productId == productId }
}
