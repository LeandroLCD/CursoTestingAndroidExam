package com.aristidevs.cursotestingandroid.checkout.domain.repository

import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation

interface OrderRepository {

    suspend fun placeOrder(): OrderConfirmation
}
