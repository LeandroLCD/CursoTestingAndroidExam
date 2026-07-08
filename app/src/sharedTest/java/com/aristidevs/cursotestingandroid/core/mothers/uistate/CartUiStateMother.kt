package com.aristidevs.cursotestingandroid.core.mothers.uistate

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.cart.domain.model.CartSummary
import com.aristidevs.cursotestingandroid.cart.presentation.CartUiState
import com.aristidevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.aristidevs.cursotestingandroid.core.mothers.PromotionMother.percent
import com.aristidevs.cursotestingandroid.productlist.domain.model.Product
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

object CartUiStateMother {
    fun cartSuccess(
        cartItems: List<CartItemWithPromotion> =
            listOf(
                cartItemWithPromotion(product = bread(), quantity = 2),
                cartItemWithPromotion(product = coffee(), quantity = 1, promotion = percent()),
            ),
        summary: CartSummary =
            CartSummary(
                subtotal = 10.3,
                discountTotal = 0.7,
                finalTotal = 11.0,
            ),
        isLoading: Boolean = false,
    ) = CartUiState.Success(
        summary = summary,
        isLoading = isLoading,
        cartItems = cartItems,
    )

    fun cartItemWithPromotion(
        product: Product,
        quantity: Int,
        promotion: ProductPromotion? = null,
    ) = CartItemWithPromotion(
        cartItem = CartItem(productId = product.id, quantity = quantity),
        item = ProductWithPromotion(product, promotion),
    )
}
