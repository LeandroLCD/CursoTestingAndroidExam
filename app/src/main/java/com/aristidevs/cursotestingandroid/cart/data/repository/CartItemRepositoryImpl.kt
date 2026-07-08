package com.aristidevs.cursotestingandroid.cart.data.repository

import com.aristidevs.cursotestingandroid.cart.data.mapper.toDomain
import com.aristidevs.cursotestingandroid.cart.data.mapper.toEntity
import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.productlist.data.local.LocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartItemRepositoryImpl
    @Inject
    constructor(
        private val localDataSource: LocalDataSource,
    ) : CartItemRepository {
        override fun getCartItems(): Flow<List<CartItem>> =
            localDataSource
                .getAllCartItems()
                .map { entities -> entities.map { it.toDomain() } }

        override suspend fun addToCart(
            productId: String,
            quantity: Int,
        ) {
            val existingItem = localDataSource.getCartItemById(productId)
            if (existingItem != null) {
                val newQuantity = existingItem.quantity + quantity
                localDataSource.updateCartItem(existingItem.copy(quantity = newQuantity))
            } else {
                localDataSource.insertCartItem(CartItem(productId, quantity).toEntity())
            }
        }

        override suspend fun removeFromCart(productId: String) {
            val item = localDataSource.getCartItemById(productId) ?: throw AppError.NotFoundError
            localDataSource.deleteCartItem(item)
        }

        override suspend fun updateQuantity(
            productId: String,
            quantity: Int,
        ) {
            val item = localDataSource.getCartItemById(productId) ?: throw AppError.NotFoundError
            localDataSource.updateCartItem(item.copy(quantity = quantity))
        }

        override suspend fun clearCart() {
            localDataSource.clearCart()
        }

        override suspend fun getCartItemById(productId: String): CartItem? = localDataSource.getCartItemById(productId)?.toDomain()
    }
