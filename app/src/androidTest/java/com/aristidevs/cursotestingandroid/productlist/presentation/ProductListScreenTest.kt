package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.milk
import com.aristidevs.cursotestingandroid.core.mothers.uistate.ProductListUiStateMother
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.FILTER_VIEW
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_LIST_LOADING
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_BADGE
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_CART
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_FILTER
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_SETTINGS
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.productListCategory
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.productListItem
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.productListSort
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createProductListScreen(
        uiState: ProductListUiState = ProductListUiStateMother.success(),
        cartItemCount: Int = 0,
        filterVisible: Boolean = true,
        onFilterSelected: (Boolean) -> Unit = {},
        onSettingsSelected: () -> Unit = {},
        onCartSelected: () -> Unit = {},
        onCategorySelected: (String?) -> Unit = {},
        onSortOptionSelected: (SortOption) -> Unit = {},
        onProductSelected: (ProductWithPromotion) -> Unit = {},
    ) {
        composeRule.setContent {
            ProductListContent(
                uiState = uiState,
                cartItemCount = cartItemCount,
                filterVisible = filterVisible,
                onFilterSelected = onFilterSelected,
                onSettingsSelected = onSettingsSelected,
                onCartSelected = onCartSelected,
                onCategorySelected = onCategorySelected,
                onSortOptionSelected = onSortOptionSelected,
                onProductSelected = onProductSelected,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgressView() {
        createProductListScreen(uiState = ProductListUiState.Loading)

        composeRule.onNodeWithTag(PRODUCT_LIST_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRendered_thenShowsErrorMessage() {
        createProductListScreen(uiState = ProductListUiState.Error(""))

        composeRule.onNodeWithText("ERROR").assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsProductsAndCount() {
        createProductListScreen(ProductListUiStateMother.success())

        composeRule.onNodeWithText("3 productos").assertIsDisplayed()
        composeRule.onNodeWithTag(FILTER_VIEW).assertIsDisplayed()

        composeRule.onNodeWithTag(productListItem(coffee().id)).assertIsDisplayed()
        composeRule.onNodeWithTag(productListItem(bread().id)).assertIsDisplayed()
        composeRule.onNodeWithTag(productListItem(milk().id)).assertIsDisplayed()

//        composeRule.onNodeWithTag(PRODUCT_LIST_LIST).performScrollToIndex(6)
//        composeRule.onNodeWithTag(PRODUCT_LIST_LIST).performScrollToNode(hasTestTag(productListItem("1234556")))
//        composeRule.onNodeWithTag(productListItem("1234556")).assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsEmptyMessage() {
        createProductListScreen(ProductListUiStateMother.success(products = emptyList()))

        composeRule.onNodeWithText("No se encontraron productos").assertIsDisplayed()
    }

    @Test
    fun givenNoCategorySelected_whenRendered_thenMarkAllChip() {
        createProductListScreen(ProductListUiStateMother.success(selectedCategory = null))

        composeRule.onNodeWithTag(productListCategory(null)).assertIsSelected()
    }

    @Test
    fun givenCategorySelected_whenRendered_thenMarkThatChip() {
        createProductListScreen(ProductListUiStateMother.success(selectedCategory = "drinks"))

        composeRule.onNodeWithTag(productListCategory("drinks")).assertIsSelected()
    }

    @Test
    fun givenSortOptionSelected_whenRendered_thenMarksThatChip() {
        createProductListScreen(uiState = ProductListUiStateMother.success(sortOption = SortOption.DISCOUNT))

        composeRule
            .onNodeWithTag(productListSort(SortOption.DISCOUNT.name))
            .assertIsSelected()
    }

    @Test
    fun givenCartItemCountZero_whenRendered_thenHidesBadge() {
        createProductListScreen(cartItemCount = 0)
        composeRule.onNodeWithTag(TOP_APP_BAR_BADGE).assertDoesNotExist()
    }

    @Test
    fun givenCartItemCountPositive_whenRendered_thenShowsBadgeWithCount() {
        createProductListScreen(cartItemCount = 67)
        composeRule.onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("67").assertIsDisplayed()
    }

    @Test
    fun givenCartItemCountOver99_whenRendered_thenShows99Plus() {
        createProductListScreen(cartItemCount = 345)
        composeRule.onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("99+").assertIsDisplayed()
    }

    @Test
    fun givenFiltersVisible_whenToggleClicked_thenEmitFalse() {
        var emitted: Boolean? = null
        createProductListScreen(
            filterVisible = true,
            onFilterSelected = { emitted = it },
        )

        composeRule.onNodeWithTag(TOP_APP_BAR_FILTER).performClick()
        assertEquals(false, emitted)
    }

    @Test
    fun givenFiltersHidden_whenToggleClicked_thenEmitTrue() {
        var emitted: Boolean? = null
        createProductListScreen(
            filterVisible = false,
            onFilterSelected = { emitted = it },
        )

        composeRule.onNodeWithTag(TOP_APP_BAR_FILTER).performClick()
        assertEquals(true, emitted)
    }

    @Test
    fun givenProductListRendered_whenSettingsIconClicked_thenEmitCallback() {
        var settingClicked = false
        createProductListScreen(onSettingsSelected = { settingClicked = true })

        composeRule.onNodeWithTag(TOP_APP_BAR_SETTINGS).performClick()
        assertTrue(settingClicked)
    }

    @Test
    fun givenProductListRendered_whenCartIconClicked_thenEmitCallback() {
        var cartClicked = false
        createProductListScreen(onCartSelected = { cartClicked = true })

        composeRule.onNodeWithTag(TOP_APP_BAR_CART).performClick()
        assertTrue(cartClicked)
    }

    @Test
    fun givenProductListRendered_whenSortDiscountClick_thenEmitsSortDiscountOption() {
        var selectedSort: SortOption? = null
        createProductListScreen(onSortOptionSelected = { newSort -> selectedSort = newSort })

        composeRule.onNodeWithTag(productListSort(SortOption.DISCOUNT.name)).performClick()

        assertEquals(SortOption.DISCOUNT, selectedSort)
    }

    @Test
    fun givenProductListRendered_whenSortPriceDescClick_thenEmitsSortPriceDescOption() {
        var selectedSort: SortOption? = null
        createProductListScreen(onSortOptionSelected = { newSort -> selectedSort = newSort })

        composeRule.onNodeWithTag(productListSort(SortOption.PRICE_DESC.name)).performClick()

        assertEquals(SortOption.PRICE_DESC, selectedSort)
    }

    @Test
    fun givenProductListRendered_whenSortPriceAscClick_thenEmitsSortPriceAscOption() {
        var selectedSort: SortOption? = null
        createProductListScreen(onSortOptionSelected = { newSort -> selectedSort = newSort })

        composeRule.onNodeWithTag(productListSort(SortOption.PRICE_ASC.name)).performClick()

        assertEquals(SortOption.PRICE_ASC, selectedSort)
    }
}
