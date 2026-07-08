package com.aristidevs.cursotestingandroid.checkout.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import jakarta.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartItemRepository: CartItemRepository
) {

    suspend operator fun invoke(): Result<OrderConfirmation> {
        return try {
            val confirmation = orderRepository.placeOrder()
            cartItemRepository.clearCart()
            Result.success(confirmation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
