package com.aristidevs.cursotestingandroid.core.mothers.uistate

import com.aristidevs.cursotestingandroid.core.mothers.ProductMother
import com.aristidevs.cursotestingandroid.core.mothers.PromotionMother
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.presentation.ProductListUiState

object ProductListUiStateMother {
    fun success(
        products: List<ProductWithPromotion> =
            listOf(
                ProductWithPromotion(ProductMother.coffee(), PromotionMother.percent()),
                ProductWithPromotion(ProductMother.bread()),
                ProductWithPromotion(ProductMother.milk()),
            ),
        categories: List<String> = listOf("bread", "drinks", "lacteo"),
        selectedCategory: String? = null,
        sortOption: SortOption = SortOption.NONE,
    ) = ProductListUiState.Success(products, categories, selectedCategory, sortOption)
}
