package com.aristidevs.cursotestingandroid.productlist.presentation

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.aristidevs.cursotestingandroid.productlist.presentation.ProductListUiState.Success
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakeProduct: ProductRepository = FakeProductRepository(),
        fakeSettings: FakeSettingsRepository = FakeSettingsRepository(),
        fakePromotion: FakePromotionRepository = FakePromotionRepository(),
        fakeClock: FakeSystemClock = FakeSystemClock(),
    ): ProductListViewModel {
        val getProductUseCase = GetProductsUseCase(
            fakeProduct,
            fakePromotion,
            GetPromotionForProduct(),
            fakeSettings,
            fakeClock,
        )
        return ProductListViewModel(
            getProductsUseCase = getProductUseCase,
            settingsRepository = fakeSettings,
        )
    }

    @Test
    fun `given products when initialized then emits success state`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val productId = "id1"
        val p1 = product { withId(productId) }
        val fakeProduct = FakeProductRepository().apply { setProducts(listOf(p1)) }

        // When
        val viewModel = createViewModel(fakeProduct = fakeProduct)

        // THEN
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is Success) // ERROR
            assertEquals(1, (state as Success).products.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given selected category when set category then filters products`() = runTest(mainDispatcherRule.scheduler) {
        val p1 = product {
            withId("1")
            withCategory("carne")
        }
        val p2 = product {
            withId("2")
            withCategory("pasta")
        }
        val fakeProduct = FakeProductRepository().apply { setProducts(listOf(p1, p2)) }

        val viewModel = createViewModel(fakeProduct = fakeProduct)

        viewModel.uiState.test {
            awaitItem()

            viewModel.setCategory("pasta")

            val state = awaitItem()

            assertTrue(state is Success)
            assertEquals(1, (state as Success).products.size)
            assertEquals("pasta", (state).selectedCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given price asc sort option when set sort option then sorts by effective price`() =
        runTest(mainDispatcherRule.scheduler) {
            val p1 = product {
                withId("1")
                withPrice(30.0)
            }
            val p2 = product {
                withId("2")
                withPrice(15.0)
            }
            val fakeProduct = FakeProductRepository().apply { setProducts(listOf(p1, p2)) }

            val viewModel = createViewModel(fakeProduct = fakeProduct)

            viewModel.uiState.test {
                awaitItem()

                viewModel.setSortOption(SortOption.PRICE_ASC)

                val state = awaitItem() as Success

                assertEquals(15.0, state.products[0].product.price, 0.0)
                assertEquals(30.0, state.products[1].product.price, 0.0)
                assertEquals(SortOption.PRICE_ASC, state.sortOption)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given repository error when loading products then emits error state`() =
        runTest(mainDispatcherRule.scheduler) {
            val failingRepository = FailingProductRepositoryStub(Exception("Prueba test"))

            val viewModel = createViewModel(fakeProduct = failingRepository)

            viewModel.uiState.test {
                val state = awaitItem()

                assertTrue(state is ProductListUiState.Error)
                assertTrue((state as ProductListUiState.Error).message == "Prueba test")

                cancelAndIgnoreRemainingEvents()
            }
        }
}
