package com.aristidevs.cursotestingandroid

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import com.aristidevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(fakeSettingsRepository: SettingsRepository = FakeSettingsRepository()) =
        MainViewModel(fakeSettingsRepository)

    @Test
    fun `given repository with dark mode when initialized then emits dark theme mode`() =
        runTest(mainDispatcherRule.scheduler) {
            val fakeSettingsRepository =
                FakeSettingsRepository().apply { setThemeMode(ThemeMode.DARK) }
            val viewModel = createViewModel(fakeSettingsRepository)

            viewModel.themeMode.test {
                assertEquals(ThemeMode.DARK, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given default repository when initialized then emits system theme mode`() =
        runTest(mainDispatcherRule.scheduler) {
            val viewModel = createViewModel()

            viewModel.themeMode.test {
                assertEquals(ThemeMode.SYSTEM, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
