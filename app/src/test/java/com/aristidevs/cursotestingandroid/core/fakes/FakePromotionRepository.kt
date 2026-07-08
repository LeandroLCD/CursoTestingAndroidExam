package com.aristidevs.cursotestingandroid.core.fakes

import com.aristidevs.cursotestingandroid.productlist.domain.model.Promotion
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePromotionRepository : PromotionRepository {
    private val promotions = MutableStateFlow<List<Promotion>>(emptyList())

    fun setPromotions(promotions: List<Promotion>) {
        this.promotions.value = promotions
    }

    override fun getActivePromotions(): Flow<List<Promotion>> = promotions.asStateFlow()

    override suspend fun refreshPromotions() {}
}
