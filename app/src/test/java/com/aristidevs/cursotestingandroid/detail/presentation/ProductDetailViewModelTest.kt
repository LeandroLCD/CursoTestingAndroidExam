package com.aristidevs.cursotestingandroid.detail.presentation

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeProduct = FakeProductRepository()
    private val fakeCart = FakeCartItemRepository()
    private val fakePromotion = FakePromotionRepository()
    private val fakeClock: FakeSystemClock = FakeSystemClock()

    private fun createViewModel() =
        ProductDetailViewModel(
            getProductDetailWithPromotionUseCase =
                GetProductDetailWithPromotionUseCase(
                    fakeProduct,
                    fakePromotion,
                    GetPromotionForProduct(),
                    fakeClock,
                ),
            addToCartUseCase = AddToCartUseCase(fakeCart, fakeProduct),
        )

    @Test
    fun `given valid product id when load product then emits item`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val p =
                product {
                    withId("1")
                    withName("erich")
                }
            fakeProduct.setProducts(listOf(p))
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()

                // when
                viewModel.loadProduct("1")

                // THEN
                val finalState = awaitItem()

                assertEquals("1", finalState.item?.product?.id)
                assertEquals("erich", finalState.item?.product?.name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given missing product id when load product then ends with item null`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            fakeProduct.setProducts(emptyList())
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()

                viewModel.loadProduct("afoafwaf")

                val state = awaitItem()

                assertNull(state.item)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given loaded product wgeb add to cart succeeds then emits success event`() =
        runTest(mainDispatcherRule.scheduler) {
            val p =
                product {
                    withId("1")
                    withStock(10)
                }
            fakeProduct.setProducts(listOf(p))
            val viewModel = createViewModel()

            viewModel.loadProduct("1")

            viewModel.events.test {
                viewModel.addToCart()
                val result = awaitItem()
                assertEquals(ProductDetailEvent.SUCCESS_ADD_TO_CART, result)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given loaded product without stock when add to cart then emits insufficient stock error`() =
        runTest(mainDispatcherRule.scheduler) {
            val p =
                product {
                    withId("1")
                    withStock(0)
                }
            fakeProduct.setProducts(listOf(p))
            val viewModel = createViewModel()

            viewModel.loadProduct("1")

            viewModel.events.test {
                viewModel.addToCart()
                val result = awaitItem()
                assertEquals(ProductDetailEvent.INSUFFICIENT_STOCK_ERROR, result)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
