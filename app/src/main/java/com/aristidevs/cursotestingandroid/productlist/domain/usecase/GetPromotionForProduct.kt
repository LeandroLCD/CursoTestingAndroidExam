package com.aristidevs.cursotestingandroid.productlist.domain.usecase

import com.aristidevs.cursotestingandroid.core.presentation.ex.roundTo2Decimals
import com.aristidevs.cursotestingandroid.productlist.domain.model.Product
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.Promotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.PromotionType
import javax.inject.Inject

class GetPromotionForProduct
    @Inject
    constructor() {
        operator fun invoke(
            product: Product,
            promotions: List<Promotion>,
        ): ProductPromotion? {
            val productPromos = promotions.filter { it.productIds.contains(product.id) }

            val buyPayPromo = productPromos.firstOrNull { it.type == PromotionType.BUY_X_PAY_Y }
            if (buyPayPromo != null) {
                val buy = buyPayPromo.buyQuantity ?: return null
                val pay = buyPayPromo.value.toInt().coerceIn(0, buy)

                return ProductPromotion.BuyXPayY(
                    buy = buy,
                    pay = pay,
                    label = "${buy}x$pay",
                )
            }

            val percentPromo =
                productPromos
                    .filter { it.type == PromotionType.PERCENT }
                    .maxByOrNull { it.value }

            if (percentPromo != null) {
                val percent = percentPromo.value.coerceIn(0.0, 100.0)
                val discountedPrice = (product.price * (1 - percent / 100.0)).roundTo2Decimals()
                return ProductPromotion.Percent(percent = percent, discountedPrice = discountedPrice)
            }

            return null
        }
    }
