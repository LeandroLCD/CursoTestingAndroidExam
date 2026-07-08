package com.aristidevs.cursotestingandroid.productlist.domain.repository

import com.aristidevs.cursotestingandroid.productlist.domain.model.Promotion
import kotlinx.coroutines.flow.Flow

interface PromotionRepository {
    fun getActivePromotions(): Flow<List<Promotion>>

    suspend fun refreshPromotions()
}
