package com.aristidevs.cursotestingandroid.productlist.domain.usecase

import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.PromotionType
import org.junit.Assert.*
import org.junit.Test

class GetPromotionForProductTest {
    private val useCase = GetPromotionForProduct()

    @Test
    fun given_no_promotions_when_invoke_then_returns_null() {
        // Given
        val product = product()

        // When
        val response = useCase(product, emptyList())

        // Then
        assertNull(response)
    }

    @Test
    fun given_percent_promotion_when_invoke_then_returns_discounted_price_rounded_to_2_decimals() {
        // Given
        val productId = "product-id"
        val product =
            product {
                withPrice(10.0)
                withId(productId)
            }
        val promotion =
            promotion {
                withType(PromotionType.PERCENT)
                withProductIds(listOf(productId))
                withValue(15.0)
            }

        // When
        val response = useCase(product, listOf(promotion))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(8.50, response.discountedPrice, 0.001)
        assertEquals(15.0, response.percent, 0.001)
    }

    @Test
    fun given_buy_x_pay_y_and_percent_promotions_when_invoke_then_prioritizes_buy_x_pay_y() {
        // Given
        val productId = "product-id"
        val product =
            product {
                withPrice(10.0)
                withId(productId)
            }
        val promotionPercent =
            promotion {
                withType(PromotionType.PERCENT)
                withProductIds(listOf(productId))
                withValue(15.0)
            }
        val promotionBuyXPayY =
            promotion {
                withType(PromotionType.BUY_X_PAY_Y)
                withProductIds(listOf(productId))
                withBuyQuantity(3)
                withValue(2.0)
            }

        // when
        val response = useCase(product, listOf(promotionPercent, promotionBuyXPayY))

        // Then
        assertTrue(response is ProductPromotion.BuyXPayY)
        response as ProductPromotion.BuyXPayY
        assertEquals(3, response.buy)
        assertEquals(2, response.pay)
        assertEquals("3x2", response.label)
    }

    @Test
    fun given_multiple_percent_promotions_then_invoke_then_returns_highest_discount() {
        // Given
        val productId = "product-id"
        val product =
            product {
                withPrice(10.0)
                withId(productId)
            }
        val promotionLow =
            promotion {
                withType(PromotionType.PERCENT)
                withProductIds(listOf(productId))
                withValue(5.0)
            }
        val promotionHigh =
            promotion {
                withType(PromotionType.PERCENT)
                withProductIds(listOf(productId))
                withValue(50.0)
            }

        // when
        val response = useCase(product, listOf(promotionHigh, promotionLow))

        // when
        assertTrue(response is ProductPromotion.Percent)
        assertEquals(50.0, (response as ProductPromotion.Percent).percent, 0.001)
    }

    @Test
    fun given_buy_x_pay_y_without_buy_quantity_when_invoke_then_returns_null() {
        // Given
        val productId = "product-id"
        val product =
            product {
                withPrice(10.0)
                withId(productId)
            }
        val promotionLow =
            promotion {
                withType(PromotionType.PERCENT)
                withProductIds(listOf(productId))
                withValue(5.0)
            }
        val brokenBuyXPromotion =
            promotion {
                withType(PromotionType.BUY_X_PAY_Y)
                withProductIds(listOf(productId))
                withBuyQuantity(null)
            }

        // when
        val response = useCase(product, listOf(promotionLow, brokenBuyXPromotion))

        // then
        assertNull(response)
    }
}
