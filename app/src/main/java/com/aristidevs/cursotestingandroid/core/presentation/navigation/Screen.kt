package com.aristidevs.cursotestingandroid.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object ProductList : Screen

    @Serializable
    data object Cart : Screen

    @Serializable
    data object Setting : Screen

    @Serializable
    data class ProductDetail(
        val productId: String,
    ) : Screen

    @Serializable
    data object Checkout: Screen
}
