package com.aristidevs.cursotestingandroid.checkout.data.repository.fake

import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository

class FakeOrderRepository:OrderRepository {
    private var orderConfirmation: OrderConfirmation? = null

    private var throwable: Throwable? = null

    var invocationCount: Int = 0
        private set

    override suspend fun placeOrder(): OrderConfirmation {
        invocationCount++
        throwable?.let { throw it }
        return orderConfirmation ?: throw Exception("Order not placed")
    }

    fun setOrderConfirmation(orderConfirmation: OrderConfirmation) {
        this.orderConfirmation = orderConfirmation
    }

    fun setThrowable(throwable: Throwable) {
        this.throwable = throwable
    }
}
