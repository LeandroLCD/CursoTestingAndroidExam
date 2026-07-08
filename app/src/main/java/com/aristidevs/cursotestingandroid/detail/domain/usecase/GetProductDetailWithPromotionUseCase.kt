package com.aristidevs.cursotestingandroid.detail.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.ex.activeAt
import com.aristidevs.cursotestingandroid.core.domain.util.Clock
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetProductDetailWithPromotionUseCase
    @Inject
    constructor(
        private val productRepository: ProductRepository,
        private val promotionRepository: PromotionRepository,
        private val getPromotionForProduct: GetPromotionForProduct,
        private val clock: Clock,
    ) {
        operator fun invoke(productId: String): Flow<ProductWithPromotion?> =
            combine(
                productRepository.getProductById(productId),
                promotionRepository.getActivePromotions(),
            ) { product, promotions ->
                val now = clock.now()
                val activePromotions = promotions.activeAt(now)

                product?.let {
                    val finalPromotion = getPromotionForProduct(it, activePromotions)
                    ProductWithPromotion(product = it, promotion = finalPromotion)
                }
            }
    }
