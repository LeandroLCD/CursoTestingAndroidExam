package com.aristidevs.cursotestingandroid.checkout.data.repository

import com.aristidevs.cursotestingandroid.checkout.data.mapper.toDomain
import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.aristidevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import jakarta.inject.Inject

class OrderRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: RemoteDataSource,
    ) : OrderRepository {
        override suspend fun placeOrder(): OrderConfirmation {
            return remoteDataSource.placeOrder().getOrThrow().toDomain()
        }
    }
