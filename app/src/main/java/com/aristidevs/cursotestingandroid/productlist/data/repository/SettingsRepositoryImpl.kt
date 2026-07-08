package com.aristidevs.cursotestingandroid.productlist.data.repository

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        companion object {
            private val IN_STOCK_ONLY_KEY = booleanPreferencesKey("IN_STOCK_ONLY_KEY")
            private val FILTERS_VISIBLE_KEY = booleanPreferencesKey("FILTERS_VISIBLE_KEY")
            private val SELECTED_CATEGORY_KEY = stringPreferencesKey("SELECTED_CATEGORY_KEY")
            private val THEME_MODE_KEY = intPreferencesKey("THEME_MODE_KEY")
            private val SORT_OPTION_KEY = stringPreferencesKey("SORT_OPTION_KEY")
        }

        private val dataStoreFlow: Flow<Preferences> =
            dataStore.data.catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }

        override val inStockOnly: Flow<Boolean> =
            dataStoreFlow.map { preferences -> preferences[IN_STOCK_ONLY_KEY] ?: false }

        override val selectedCategory: Flow<String?> =
            dataStoreFlow.map { preferences -> preferences[SELECTED_CATEGORY_KEY] }

        override val filtersVisible: Flow<Boolean> =
            dataStoreFlow.map { preferences -> preferences[FILTERS_VISIBLE_KEY] ?: true }

        override val themeMode: Flow<ThemeMode> =
            dataStoreFlow.map { preferences ->
                when (preferences[THEME_MODE_KEY]) {
                    ThemeMode.SYSTEM.id -> ThemeMode.SYSTEM
                    ThemeMode.LIGHT.id -> ThemeMode.LIGHT
                    ThemeMode.DARK.id -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
            }

        override val sortOption: Flow<SortOption> =
            dataStoreFlow.map { preferences ->
                val raw = preferences[SORT_OPTION_KEY]
                runCatching {
                    SortOption.valueOf(
                        raw ?: SortOption.NONE.name,
                    )
                }.getOrDefault(SortOption.NONE)
            }

        override suspend fun setInStockOnly(value: Boolean) {
            dataStore.edit { preferences ->
                preferences[IN_STOCK_ONLY_KEY] = value
            }
        }

        override suspend fun setThemeMode(value: ThemeMode) {
            dataStore.edit { preferences ->
                when (value) {
                    ThemeMode.DARK -> preferences[THEME_MODE_KEY] = ThemeMode.DARK.id
                    ThemeMode.LIGHT -> preferences[THEME_MODE_KEY] = ThemeMode.LIGHT.id
                    ThemeMode.SYSTEM -> preferences[THEME_MODE_KEY] = ThemeMode.SYSTEM.id
                }
            }
        }

        override suspend fun setSelectedCategory(value: String?) {
            dataStore.edit { preferences ->
                if (value == null) {
                    preferences.remove(SELECTED_CATEGORY_KEY)
                } else {
                    preferences[SELECTED_CATEGORY_KEY] = value
                }
            }
        }

        override suspend fun setFiltersVisible(value: Boolean) {
            dataStore.edit { preferences ->
                preferences[FILTERS_VISIBLE_KEY] = value
            }
        }

        override suspend fun setSortOption(value: SortOption) {
            dataStore.edit { preferences ->
                preferences[SORT_OPTION_KEY] = value.name
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        suspend fun clear() {
            dataStore.edit { it.clear() }
            dataStore.data.first { it.asMap().isEmpty() }
        }
    }
