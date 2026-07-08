package com.aristidevs.cursotestingandroid.settings.presentation

import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode

data class SettingsUiState(
    val inStockOnly: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
