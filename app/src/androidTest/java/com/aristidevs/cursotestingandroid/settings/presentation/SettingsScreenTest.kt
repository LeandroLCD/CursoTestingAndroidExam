package com.aristidevs.cursotestingandroid.settings.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_CONTENT
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_IN_STOCK_SWITCH
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_TAX_SWITCH
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.settingsThemeOption
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createSettingsScreen(
        uiState: SettingsUiState = SettingsUiState(),
        onBack: () -> Unit = {},
        onThemeModeSelected: (ThemeMode) -> Unit = {},
        onInStockOnlyChange: (Boolean) -> Unit = {},
    ) {
        composeRule.setContent {
            SettingsContent(
                uiState = uiState,
                onBack = onBack,
                onThemeModeSelected = onThemeModeSelected,
                onInStockOnlyChange = onInStockOnlyChange,
            )
        }
    }

    private fun getString(resId: Int): String = composeRule.activity.getString(resId)

    @Test
    fun givenDefaultSettingsState_whenRendered_thenShowsFilterAndAppearanceSections() {
        createSettingsScreen(uiState = SettingsUiState())

        composeRule.onNodeWithText(getString(R.string.settings_title)).assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_filters_section)).assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_in_stock_only)).assertIsDisplayed()
        composeRule
            .onNodeWithText(getString(R.string.settings_appearance_section))
            .assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_theme_label)).assertIsDisplayed()

        composeRule.onNodeWithTag(SETTINGS_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(SETTINGS_TAX_SWITCH).assertIsOn()
    }

    @Test
    fun givenInStockOnlyFalse_whenRendered_thenSwitchIsOff() {
        createSettingsScreen(uiState = SettingsUiState(inStockOnly = false))
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
    }

    @Test
    fun givenInStockOnlyTrue_whenRendered_thenSwitchIsOn() {
        createSettingsScreen(uiState = SettingsUiState(inStockOnly = true))
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOn()
    }

    @Test
    fun givenLightTheme_whenRendered_thenLightOptionIsSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.LIGHT))
        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("light")).assertIsSelected()
    }

    @Test
    fun givenSystemTheme_whenRendered_thenSystemOptionIsSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.SYSTEM))
        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("System")).assertIsSelected()
    }

    @Test
    fun givenDarkTheme_whenRendered_thenDarkOptionIsSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.DARK))
        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("Dark")).assertIsSelected()
    }

    @Test
    fun givenSettingsRendered_whenBackClicked_thenEmitBackCallback() {
        var backClicked = false

        createSettingsScreen(
            onBack = { backClicked = true },
        )

        composeRule.onNodeWithTag(TOP_APP_BAR).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun givenInStockSwitchOff_whenClicked_thenEmitsTrue() {
        var emitted: Boolean? = null

        createSettingsScreen(
            uiState = SettingsUiState(inStockOnly = false),
            onInStockOnlyChange = { newState -> emitted = newState },
        )

        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).performClick()

        assertEquals(true, emitted)
    }

    @Test
    fun givenLightTheme_whenDarkClicked_thenEmitsDarkTheme() {
        var selectedTheme: ThemeMode? = null

        createSettingsScreen(
            uiState = SettingsUiState(themeMode = ThemeMode.LIGHT),
            onThemeModeSelected = { newThemeMode -> selectedTheme = newThemeMode },
        )

        composeRule.onNodeWithTag(settingsThemeOption("dark")).performClick()

        assertEquals(ThemeMode.DARK, selectedTheme)
    }
}
