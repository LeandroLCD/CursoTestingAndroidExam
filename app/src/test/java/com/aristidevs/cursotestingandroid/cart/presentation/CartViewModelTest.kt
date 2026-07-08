package com.aristidevs.cursotestingandroid.cart.presentation

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.domain.util.Clock
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        productRepository: ProductRepository = FakeProductRepository(),
        cartItemRepository: CartItemRepository = FakeCartItemRepository(),
        promotionRepository: PromotionRepository = FakePromotionRepository(),
        clock: Clock = FakeSystemClock(),
    ): CartViewModel {
        val getCartSummaryUseCase =
            GetCartSummaryUseCase(
                cartItemRepository,
                productRepository,
                promotionRepository,
                GetPromotionForProduct(),
                clock,
            )

        val updateCartItemUseCase = UpdateCartItemUseCase(cartItemRepository, productRepository)

        val getCartItemsWithPromotionsUseCase =
            GetCartItemsWithPromotionsUseCase(
                cartItemRepository,
                productRepository,
                promotionRepository,
                GetPromotionForProduct(),
                clock,
            )

        return CartViewModel(
            cartItemRepository,
            getCartSummaryUseCase,
            updateCartItemUseCase,
            getCartItemsWithPromotionsUseCase,
        )
    }

    @Test
    fun `given cart data when initialized then emit success state`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val productId = "1"
            val p =
                product {
                    withId(productId)
                    withName("Pan")
                    withPrice(2.0)
                }
            val item =
                cartItem {
                    withProductId(productId)
                    withQuantity(3)
                }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(p)) }
            val fakeCartRepository = FakeCartItemRepository().apply { setCartItems(listOf(item)) }

            // WHEN
            val viewModel = createViewModel(fakeProductRepository, fakeCartRepository)

            // THEN
            viewModel.uiState.test {
                val state = awaitItem() as CartUiState.Success
                assertEquals(1, state.cartItems.size)
                assertEquals(6.0, state.summary?.subtotal)
            }
        }

    @Test
    fun `given quantity one when decrease quantity then removes item from cart`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val productId = "1"
            val p =
                product {
                    withId(productId)
                    withStock(5)
                    withPrice(2.0)
                }
            val item =
                cartItem {
                    withProductId(productId)
                    withQuantity(1)
                }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(p)) }
            val fakeCartRepository = FakeCartItemRepository().apply { setCartItems(listOf(item)) }
            val viewModel = createViewModel(fakeProductRepository, fakeCartRepository)

            viewModel.uiState.test {
                awaitItem()

                // WHEN
                viewModel.decreaseQuantity(productId, 1)

                // THEN
                val state = awaitItem() as CartUiState.Success
                assertTrue(state.cartItems.isEmpty())
                assertEquals(0.0, state.summary?.finalTotal ?: 0.0, 0.001)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given insufficient stock when update quantity then emits error event`() =
        runTest(mainDispatcherRule.scheduler) {
            val productId = "1"
            val p =
                product {
                    withId(productId)
                    withStock(2)
                }
            val item =
                cartItem {
                    withProductId(productId)
                    withQuantity(1)
                }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(p)) }
            val fakeCartRepository = FakeCartItemRepository().apply { setCartItems(listOf(item)) }
            val viewModel = createViewModel(fakeProductRepository, fakeCartRepository)

            viewModel.event.test {
                viewModel.increaseQuantity(productId, 5)

                val event = awaitItem()
                assertTrue(event is CartEvent.ShowMessage)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
