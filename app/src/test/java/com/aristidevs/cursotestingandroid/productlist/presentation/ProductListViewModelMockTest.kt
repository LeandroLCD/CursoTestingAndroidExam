package com.aristidevs.cursotestingandroid.productlist.presentation

import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProductListViewModelMockTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository =
        mockk(relaxed = true) {
            every { selectedCategory } returns flowOf(null)
            every { sortOption } returns flowOf(SortOption.NONE)
            every { inStockOnly } returns flowOf(false)
            every { filtersVisible } returns flowOf(true)
        }

    private fun createViewModel(
        fakeProduct: ProductRepository = FakeProductRepository(),
        fakeSettings: FakeSettingsRepository = FakeSettingsRepository(),
        fakePromotion: FakePromotionRepository = FakePromotionRepository(),
        fakeClock: FakeSystemClock = FakeSystemClock(),
    ): ProductListViewModel {
        val getProductUseCase =
            GetProductsUseCase(
                fakeProduct,
                fakePromotion,
                GetPromotionForProduct(),
                fakeSettings,
                fakeClock,
            )

        return ProductListViewModel(
            getProductsUseCase = getProductUseCase,
            settingsRepository = settingsRepository,
        )
    }

    @Test
    fun `given category when set category then delegates to settings repository`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val viewModel = createViewModel()
            val category = "pasta"

            // WHEN
            viewModel.setCategory(category)

            // THEN
            coVerify(exactly = 1) { settingsRepository.setSelectedCategory(category) }
        }

    @Test
    fun `given sort option when set sort option then delegates to settings repository`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val viewModel = createViewModel()
            val option = SortOption.DISCOUNT

            // WHEN
            viewModel.setSortOption(option)

            // THEN
            coVerify(exactly = 1) { settingsRepository.setSortOption(option) }
        }

    @Test
    fun `given filter visible when set filter visible then delegates to settings repository`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val viewModel = createViewModel()
            val option = true

            // WHEN
            viewModel.setFilterVisible(option)

            // THEN
            coVerify(exactly = 1) { settingsRepository.setFiltersVisible(option) }
        }
}
