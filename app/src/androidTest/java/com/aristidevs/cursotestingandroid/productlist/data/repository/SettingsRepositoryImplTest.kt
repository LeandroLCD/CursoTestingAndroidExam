package com.aristidevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() =
        runTest {
            hilt.inject()
            (settingsRepository as? SettingsRepositoryImpl)?.clear()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenNoDataSaved_whenInStockOnlyIsRead_thenReturnsDefaultFalse() =
        runTest {
            val result = settingsRepository.inStockOnly.first()
            assertTrue(!result)
        }

    @Test
    fun givenNoDataSaved_whenFilterVisibleIsRead_thenReturnsDefaultTrue() =
        runTest {
            val result = settingsRepository.filtersVisible.first()
            assertTrue(result)
        }

    @Test
    fun givenNoDataSaved_whenSelectCategoryIsRead_thenReturnsDefaultNull() =
        runTest {
            val result = settingsRepository.selectedCategory.first()
            assertNull(result)
        }

    @Test
    fun givenNoDataSaved_whenThemeModeIsRead_thenReturnsDefaultSystem() =
        runTest {
            assertEquals(ThemeMode.SYSTEM, settingsRepository.themeMode.first())
        }

    @Test
    fun givenNoDataSaved_whenSortOptionIsRead_thenReturnsDefaultSystem() =
        runTest {
            assertEquals(SortOption.NONE, settingsRepository.sortOption.first())
        }

    @Test
    fun givenRepository_whenSetFilterVisibleToFalse_thenPersistValue() =
        runTest {
            settingsRepository.setFiltersVisible(false)
            val result = settingsRepository.filtersVisible.first()
            assertTrue(!result)
        }

    @Test
    fun givenMultipleSettingsChanges_whenReadAll_thenStateIsConsistent() =
        runTest {
            settingsRepository.setFiltersVisible(false)
            settingsRepository.setInStockOnly(true)
            settingsRepository.setSortOption(SortOption.DISCOUNT)
            settingsRepository.setThemeMode(ThemeMode.DARK)
            settingsRepository.setSelectedCategory("papas")

            assertTrue(!settingsRepository.filtersVisible.first())
            assertEquals(ThemeMode.DARK, settingsRepository.themeMode.first())
            assertEquals("papas", settingsRepository.selectedCategory.first())
            assertEquals(SortOption.DISCOUNT, settingsRepository.sortOption.first())
            assertEquals(true, settingsRepository.inStockOnly.first())
        }
}
