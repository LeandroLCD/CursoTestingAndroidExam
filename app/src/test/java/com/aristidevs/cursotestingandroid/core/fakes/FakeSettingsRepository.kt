package com.aristidevs.cursotestingandroid.core.fakes

import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository : SettingsRepository {
    private val _inStockOnly = MutableStateFlow(false)
    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.SYSTEM)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _filtersVisible = MutableStateFlow(true)
    private val _sortOption = MutableStateFlow(SortOption.NONE)

    override val inStockOnly: Flow<Boolean> = _inStockOnly.asStateFlow()

    override val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()
    override val selectedCategory: Flow<String?> = _selectedCategory.asStateFlow()
    override val filtersVisible: Flow<Boolean> = _filtersVisible.asStateFlow()
    override val sortOption: Flow<SortOption> = _sortOption.asStateFlow()

    override suspend fun setInStockOnly(value: Boolean) {
        _inStockOnly.value = value
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        _themeMode.value = value
    }

    override suspend fun setSelectedCategory(value: String?) {
        _selectedCategory.value = value
    }

    override suspend fun setFiltersVisible(value: Boolean) {
        _filtersVisible.value = value
    }

    override suspend fun setSortOption(value: SortOption) {
        _sortOption.value = value
    }
}
