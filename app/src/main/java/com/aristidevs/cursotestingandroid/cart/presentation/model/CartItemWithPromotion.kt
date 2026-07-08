package com.aristidevs.cursotestingandroid.cart.presentation.model

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class CartItemWithPromotion(
    val cartItem: CartItem,
    val item: ProductWithPromotion,
)
