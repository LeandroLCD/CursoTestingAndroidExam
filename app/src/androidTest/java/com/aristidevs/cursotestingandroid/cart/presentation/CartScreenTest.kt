package com.aristidevs.cursotestingandroid.cart.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.aristidevs.cursotestingandroid.core.mothers.uistate.CartUiStateMother.cartItemWithPromotion
import com.aristidevs.cursotestingandroid.core.mothers.uistate.CartUiStateMother.cartSuccess
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.CART_EMPTY
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.CART_LOADING
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.CART_RETRY
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.cartItem
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.cartQuantityDecrease
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.cartQuantityIncrease
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createCartScreen(
        state: CartUiState,
        onBack: () -> Unit = {},
        onRetrySelected: () -> Unit = {},
        onIncreaseQuantity: (String, Int) -> Unit = { _, _ -> },
        onDecreaseQuantity: (String, Int) -> Unit = { _, _ -> },
        onRemove: (String) -> Unit = {},
        navigateToCheckout: () -> Unit = {},
    ) {
        composeRule.setContent {
            CartContent(
                state = state,
                onBack = onBack,
                onRetrySelected = onRetrySelected,
                onRemove = onRemove,
                onDecreaseQuantity = onDecreaseQuantity,
                onIncreaseQuantity = onIncreaseQuantity,
                navigateToCheckout = navigateToCheckout
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowProgressView() {
        createCartScreen(state = CartUiState.Loading)

        composeRule.onNodeWithTag(CART_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRendered_thenShowTextAndRetryButton() {
        val errorText = "Prueba error"
        createCartScreen(state = CartUiState.Error(errorText))

        composeRule
            .onNodeWithText(errorText, substring = true, ignoreCase = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenEmitsRetryCallback() {
        var retryClicked = false

        val errorText = "Prueba error"
        createCartScreen(
            state = CartUiState.Error(errorText),
            onRetrySelected = { retryClicked = true },
        )

        composeRule.onNodeWithTag(CART_RETRY).performClick()

        assertTrue(retryClicked)
    }

    @Test
    fun givenEmptySuccessState_whenRendered_thenShowsEmptyCartMessage() {
        createCartScreen(
            state =
                CartUiState.Success(
                    summary = null,
                    isLoading = false,
                    cartItems = emptyList(),
                ),
        )

        composeRule.onNodeWithTag(CART_EMPTY).assertIsDisplayed()
        composeRule.onNodeWithText("Tu carrito está vacío").assertIsDisplayed()
        composeRule.onNodeWithText("Agrega productos para comenzar").assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsItemsQuantitiesAndSummary() {
        createCartScreen(state = cartSuccess())

        composeRule.onNodeWithText(coffee().name).assertIsDisplayed()
        composeRule.onNodeWithText(bread().name).assertIsDisplayed()
        composeRule.onNodeWithText("Subtotal").assertIsDisplayed()
        composeRule.onNodeWithText("Descuento").assertIsDisplayed()
        composeRule.onNodeWithText("Total").assertIsDisplayed()

        composeRule.onNodeWithTag(cartItem(coffee().id)).assertIsDisplayed()
        composeRule.onNodeWithTag(cartItem(bread().id)).assertIsDisplayed()
    }

    @Test
    fun givenInitialQuantity_whenIncreaseClicked_thenEmitsIncreaseQuantity() {
        var emitted: Pair<String, Int>? = null
        val initialQuantity = 2

        createCartScreen(
            state =
                cartSuccess(
                    cartItems =
                        listOf(
                            cartItemWithPromotion(
                                product = bread(),
                                quantity = initialQuantity,
                            ),
                        ),
                ),
            onIncreaseQuantity = { productId, quantity -> emitted = productId to quantity },
        )

        composeRule
            .onNodeWithTag(cartQuantityIncrease(bread().id))
            .assertIsEnabled()
            .performClick()

        assertEquals(bread().id to initialQuantity, emitted)
    }

    @Test
    fun givenInitialQuantity_whenDecreaseClicked_thenEmitDecreaseQuantity() {
        var emitted: Pair<String, Int>? = null
        val initialQuantity = 3

        createCartScreen(
            state =
                cartSuccess(
                    cartItems =
                        listOf(
                            cartItemWithPromotion(
                                product = bread(),
                                quantity = initialQuantity,
                            ),
                        ),
                ),
            onDecreaseQuantity = { productId, quantity -> emitted = productId to quantity },
        )

        composeRule
            .onNodeWithTag(cartQuantityDecrease(bread().id))
            .assertIsEnabled()
            .performClick()

        assertEquals(bread().id to initialQuantity, emitted)
    }

    @Test
    fun givenCartItem_whenSwipedRight_thenEmitsRemoveCallback() {
        var removeProductId: String? = null

        createCartScreen(
            state =
                cartSuccess(
                    cartItems =
                        listOf(
                            cartItemWithPromotion(
                                product = bread(),
                                quantity = 2,
                            ),
                        ),
                ),
            onRemove = { removeProductId = it },
        )

        composeRule
            .onNodeWithTag(cartItem(bread().id))
            .performTouchInput { swipeRight() }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            removeProductId != null
        }

        assertEquals(bread().id, removeProductId)
    }

    @Test
    fun givenItemsAtStockEdges_whenRendered_thenInvalidControlsAreDisable() {
        val fullStockItem =
            cartItemWithPromotion(
                bread(7),
                quantity = 7,
            )

        createCartScreen(cartSuccess(cartItems = listOf(fullStockItem)))

        composeRule
            .onNodeWithTag(cartQuantityIncrease(bread().id))
            .assertIsNotEnabled()
    }
}
