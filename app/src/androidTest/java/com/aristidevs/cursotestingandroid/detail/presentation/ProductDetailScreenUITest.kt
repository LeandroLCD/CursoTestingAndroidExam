package com.aristidevs.cursotestingandroid.detail.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.milk
import com.aristidevs.cursotestingandroid.core.mothers.PromotionMother.buyXPayYDefault
import com.aristidevs.cursotestingandroid.core.mothers.PromotionMother.percent
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_BUTTON_NO_STOCK
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_LOADING
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import org.junit.Rule
import kotlin.test.Test

class ProductDetailScreenUITest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    fun createProductDetailScreen(
        uiState: ProductDetailUiState,
        onBack: () -> Unit = {},
        onAddToCart: () -> Unit = {},
    ) {
        composeRule.setContent {
            ProductDetailContent(
                uiState = uiState,
                onBack = onBack,
                onAddToCart = onAddToCart,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_showsProgress() {
        createProductDetailScreen(uiState = ProductDetailUiState(isLoading = true))

        composeRule.onNodeWithTag(PRODUCT_DETAIL_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenProductWithoutStock_whenRendered_thenShowsDisabledNoStockAction() {
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item = ProductWithPromotion(coffee(stock = 0)),
                ),
        )

        composeRule.onNodeWithText("Sin stock disponible").assertIsDisplayed()
        composeRule.onNodeWithText("Sin stock").assertIsDisplayed()
        composeRule.onNodeWithTag(PRODUCT_DETAIL_BUTTON_NO_STOCK).assertIsNotEnabled()
    }

    @Test
    fun givenBuyXPayYPromotion_whenRendered_thenShowsBadge() {
        val promotion = buyXPayYDefault()
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item =
                        ProductWithPromotion(
                            milk(),
                            promotion = promotion,
                        ),
                ),
        )

        composeRule.onNodeWithText("PROMO: ${promotion.label}").assertIsDisplayed()
    }

    @Test
    fun givenPercentPromotion_whenRendered_thenShowsOriginalPriceDiscountedPriceAndBadge() {
        val product = coffee()
        val promotion = percent()
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item =
                        ProductWithPromotion(
                            product,
                            promotion = promotion,
                        ),
                ),
        )

        composeRule.onNodeWithText(product.price.toString()).assertIsDisplayed()
        composeRule.onNodeWithText(promotion.discountedPrice.toString()).assertIsDisplayed()

        composeRule.onNodeWithText("${promotion.percent.toInt()}% OFF").assertIsDisplayed()
    }
}
