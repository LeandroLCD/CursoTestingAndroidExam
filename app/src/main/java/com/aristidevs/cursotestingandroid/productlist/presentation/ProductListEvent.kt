package com.aristidevs.cursotestingandroid.productlist.presentation

sealed interface ProductListEvent {
    data class ShowMessage(
        val message: String,
    ) : ProductListEvent
}
