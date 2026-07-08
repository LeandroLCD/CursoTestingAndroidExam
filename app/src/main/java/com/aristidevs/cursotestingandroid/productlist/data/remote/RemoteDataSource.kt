package com.aristidevs.cursotestingandroid.productlist.data.remote

import com.aristidevs.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.aristidevs.cursotestingandroid.productlist.data.remote.response.PromotionResponse
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class RemoteDataSource
    @Inject
    constructor(
        val miniMarketApiService: MiniMarketApiService,
    ) {
        suspend fun getProducts(): Result<List<ProductResponse>> =
            try {
                val response = miniMarketApiService.getProducts()
                Result.success(response.products)
            } catch (e: Exception) {
                Result.failure(mapToDomainError(e))
            }

        suspend fun getPromotions(): Result<List<PromotionResponse>> =
            try {
                val response = miniMarketApiService.getPromotions()
                Result.success(response.promotions)
            } catch (e: Exception) {
                Result.failure(mapToDomainError(e))
            }

        suspend fun placeOrder(): Result<OrderConfirmationResponse> =
            try {
                Result.success(miniMarketApiService.placeOrder())
            } catch (e: Exception) {
                Result.failure(mapToDomainError(e))
            }

        private fun mapToDomainError(e: Exception): AppError =
            when (e) {
                is UnknownHostException -> AppError.NetworkError
                is SocketTimeoutException -> AppError.NetworkError
                is IOException -> AppError.NetworkError
                is HttpException -> {
                    when (e.code()) {
                        404 -> AppError.NotFoundError
                        else -> AppError.NetworkError
                    }
                }

                else -> AppError.UnknownError(e.message)
            }
    }
