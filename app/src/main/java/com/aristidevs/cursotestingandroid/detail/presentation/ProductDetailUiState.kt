package com.aristidevs.cursotestingandroid.detail.presentation

import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class ProductDetailUiState(
    val item: ProductWithPromotion? = null,
    val isLoading: Boolean = true,
)
