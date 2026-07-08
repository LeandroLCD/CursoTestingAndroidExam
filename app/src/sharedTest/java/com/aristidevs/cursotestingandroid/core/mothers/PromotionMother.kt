package com.aristidevs.cursotestingandroid.core.mothers

import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion

object PromotionMother {
    fun percent(
        percent: Double = 25.0,
        discountedPrice: Double = 4.65,
    ) = ProductPromotion.Percent(percent = percent, discountedPrice = discountedPrice)

    fun buyXPayYDefault() = ProductPromotion.BuyXPayY(3, 2, "3x2")
}
