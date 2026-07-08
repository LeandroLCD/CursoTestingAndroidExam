package com.aristidevs.cursotestingandroid.productlist.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.ex.activeAt
import com.aristidevs.cursotestingandroid.core.domain.util.Clock
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetProductsUseCase
    @Inject
    constructor(
        private val productRepository: ProductRepository,
        private val promotionRepository: PromotionRepository,
        private val getPromotionForProduct: GetPromotionForProduct,
        private val settingsRepository: SettingsRepository,
        private val clock: Clock,
    ) {
        operator fun invoke(): Flow<List<ProductWithPromotion>> =
            combine(
                productRepository.getProducts(),
                promotionRepository.getActivePromotions(),
                settingsRepository.inStockOnly,
            ) { products, promotions, inStockOnly ->

                val now = clock.now()

                val activePromotions = promotions.activeAt(now)

                val filteredProducts =
                    if (inStockOnly) {
                        products.filter { it.stock > 0 }
                    } else {
                        products
                    }

                filteredProducts.map { product ->
                    val promotion = getPromotionForProduct(product, activePromotions)
                    ProductWithPromotion(product = product, promotion = promotion)
                }
            }
    }
