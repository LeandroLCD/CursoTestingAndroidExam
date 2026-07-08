package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.asAsset
import com.aristidevs.cursotestingandroid.productlist.data.repository.SettingsRepositoryImpl
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductListViewModelIntegrationTest {
    private companion object {
        const val EXPECTED_PRODUCT_SIZE = 3
        const val DAIRY_CATEGORY = "Lácteos"
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var getProductsUseCase: GetProductsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() =
        runTest {
            mockWebServer.server.dispatcher =
                MiniMarketApiDispatcher(
                    productJson = "product_list_default.json".asAsset(),
                )
            hilt.inject()
            (settingsRepository as? SettingsRepositoryImpl)?.clear()

            productRepository.refreshProduct()
            promotionRepository.refreshPromotions()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenSuccessfulApi_whenViewModelLoads_thenShowsProducts() =
        runTest {
            val viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)

            viewModel.uiState.test {
                val result = awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE }
                assertTrue(result.products.isNotEmpty())
                assertTrue(result.products.size == EXPECTED_PRODUCT_SIZE)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenDairyCategorySelected_whenFiltering_thenOnlyDairyProductAreShown() =
        runTest {
            val viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)

            viewModel.uiState.test {
                awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE }

                viewModel.setCategory(DAIRY_CATEGORY)

                val result =
                    awaitSuccessMatching { state ->
                        state.selectedCategory == DAIRY_CATEGORY &&
                            state.products.isNotEmpty() &&
                            state.products.all { it.product.category == DAIRY_CATEGORY }
                    }

                assertTrue(result.products.size == 2)
                assertTrue(result.products.all { it.product.category == DAIRY_CATEGORY })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenProductsLoaded_whenSortingByPriceAsc_thenListIsCorrectlyOrdered() =
        runTest {
            val viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)

            viewModel.uiState.test {
                awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE }

                viewModel.setSortOption(SortOption.PRICE_ASC)

                val result =
                    awaitSuccessMatching { state ->
                        state.sortOption == SortOption.PRICE_ASC &&
                            state.products.map { it.product.price } ==
                            state.products
                                .map { it.product.price }
                                .sorted()
                    }

                assertEquals(
                    10.0,
                    result.products
                        .first()
                        .product.price,
                )
                assertEquals(listOf(10.0, 15.0, 20.0), result.products.map { it.product.price })
                cancelAndIgnoreRemainingEvents()
            }
        }

    private suspend fun ReceiveTurbine<ProductListUiState>.awaitSuccessMatching(
        predicate: (ProductListUiState.Success) -> Boolean,
    ): ProductListUiState.Success {
        while (true) {
            when (val item = awaitItem()) {
                is ProductListUiState.Success -> if (predicate(item)) return item
                is ProductListUiState.Error -> error("Unexpected error: ${item.message}")
                is ProductListUiState.Loading -> Unit
            }
        }
    }
}
