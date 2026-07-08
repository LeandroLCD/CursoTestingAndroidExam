package com.aristidevs.cursotestingandroid.cart.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.asAsset
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartViewModelIntegrationTest {
    private companion object {
        const val PRODUCT_ID = "p1"
        const val UPDATED_QUANTITY = 2
        const val INITIAL_QUANTITY = 1
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var cartItemRepository: CartItemRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Inject
    lateinit var getCartSummaryUseCase: GetCartSummaryUseCase

    @Inject
    lateinit var updateCartItemUseCase: UpdateCartItemUseCase

    @Inject
    lateinit var getCartItemsWithPromotionsUseCase: GetCartItemsWithPromotionsUseCase

    @Before
    fun setUp() =
        runTest {
            mockWebServer.server.dispatcher =
                MiniMarketApiDispatcher(
                    productJson = "product_list_default.json".asAsset(),
                )
            hilt.inject()
            cartItemRepository.clearCart()

            productRepository.refreshProduct()
            promotionRepository.refreshPromotions()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenCartWithItems_whenViewModelCollectsUiState_thenSuccessWithSummary() =
        runTest {
            cartItemRepository.addToCart(PRODUCT_ID, UPDATED_QUANTITY)

            val viewModel = createViewModel()

            viewModel.uiState.test {
//            val result = awaitStateMatching { state ->
//                state is CartUiState.Success &&
//                        state.summary != null &&
//                        state.cartItems.isNotEmpty()
//            }
//            val success = result as CartUiState.Success

                val result =
                    awaitSuccessMatching { state ->
                        state.summary != null && state.cartItems.isNotEmpty()
                    }

                assertTrue(result.cartItems.isNotEmpty())
                assertTrue(result.summary != null)
                assertEquals(20.0, result.summary!!.subtotal, 0.01)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenSingleProduct_whenIncreaseQuantity_thenQuantityUpdates() =
        runTest {
            cartItemRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val success =
                    awaitSuccessMatching { state ->
                        state.cartItems.any {
                            it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == INITIAL_QUANTITY
                        }
                    }

                assertEquals(
                    INITIAL_QUANTITY,
                    success.cartItems
                        .first()
                        .cartItem.quantity,
                )

                viewModel.increaseQuantity(PRODUCT_ID, INITIAL_QUANTITY)

                val updatedSuccess =
                    awaitSuccessMatching { state ->
                        state.cartItems.any {
                            it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == UPDATED_QUANTITY
                        }
                    }

                assertEquals(
                    UPDATED_QUANTITY,
                    updatedSuccess.cartItems
                        .first()
                        .cartItem.quantity,
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenSingleProduct_whenDecreaseToZero_thenCartBecomesEmpty() =
        runTest {
            cartItemRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val success =
                    awaitSuccessMatching { state ->
                        state.cartItems.any {
                            it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == INITIAL_QUANTITY
                        }
                    }

                assertEquals(
                    INITIAL_QUANTITY,
                    success.cartItems
                        .first()
                        .cartItem.quantity,
                )

                viewModel.decreaseQuantity(PRODUCT_ID, INITIAL_QUANTITY)

                val emptySuccess =
                    awaitSuccessMatching { state ->
                        state.cartItems.isEmpty()
                    }

                assertTrue(emptySuccess.cartItems.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createViewModel(): CartViewModel =
        CartViewModel(
            cartItemRepository = cartItemRepository,
            getCartSummaryUseCase = getCartSummaryUseCase,
            updateCartItemUseCase = updateCartItemUseCase,
            getCartItemsWithPromotionsUseCase = getCartItemsWithPromotionsUseCase,
        )

    private suspend fun ReceiveTurbine<CartUiState>.awaitSuccessMatching(predicate: (CartUiState.Success) -> Boolean): CartUiState.Success {
        while (true) {
            when (val item = awaitItem()) {
                is CartUiState.Success -> if (predicate(item)) return item
                is CartUiState.Error -> error("Unexpected error: ${item.message}")
                is CartUiState.Loading -> Unit
            }
        }
    }
}
