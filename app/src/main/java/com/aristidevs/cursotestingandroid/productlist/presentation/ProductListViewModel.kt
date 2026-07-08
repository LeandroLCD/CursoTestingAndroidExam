package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel
    @Inject
    constructor(
        getProductsUseCase: GetProductsUseCase,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val uiState: StateFlow<ProductListUiState> =
            combine(
                getProductsUseCase(),
                settingsRepository.selectedCategory,
                settingsRepository.sortOption,
            ) { products, category, sortOption ->
                var filteredProducts = products

                if (category != null) {
                    filteredProducts = filteredProducts.filter { it.product.category == category }
                }

                val sorted =
                    when (sortOption) {
                        SortOption.PRICE_ASC -> filteredProducts.sortedBy { effectivePrice(it) }
                        SortOption.PRICE_DESC -> filteredProducts.sortedByDescending { effectivePrice(it) }
                        SortOption.NONE -> filteredProducts
                        SortOption.DISCOUNT ->
                            filteredProducts.sortedWith(
                                compareByDescending<ProductWithPromotion> {
                                    effectiveDiscountPercent(it)
                                }.thenBy { it.promotion == null },
                            )
                    }

                val categories = products.map { it.product.category }.distinct().sorted()

                ProductListUiState.Success(
                    products = sorted,
                    categories = categories,
                    selectedCategory = category,
                    sortOption = sortOption,
                ) as ProductListUiState
            }.catch { e: Throwable ->
                emit(ProductListUiState.Error(e.message.orEmpty()))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProductListUiState.Loading,
            )

        private val _events = MutableSharedFlow<ProductListEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<ProductListEvent> = _events

        val filterVisible: StateFlow<Boolean> =
            settingsRepository.filtersVisible.stateIn(
                scope = viewModelScope,
                initialValue = true,
                started = SharingStarted.WhileSubscribed(5000),
            )

        fun setCategory(category: String?) {
            viewModelScope.launch {
                settingsRepository.setSelectedCategory(category)
            }
        }

        fun setSortOption(sortOption: SortOption) {
            viewModelScope.launch {
                settingsRepository.setSortOption(sortOption)
            }
        }

        fun setFilterVisible(showFilters: Boolean) {
            viewModelScope.launch {
                settingsRepository.setFiltersVisible(showFilters)
            }
        }

        private fun effectiveDiscountPercent(item: ProductWithPromotion): Double =
            when (val promo = item.promotion) {
                is ProductPromotion.Percent -> promo.percent
                else -> 0.0
            }

        private fun effectivePrice(item: ProductWithPromotion): Double =
            when (val promo = item.promotion) {
                is ProductPromotion.Percent -> promo.discountedPrice
                else -> item.product.price
            }
    }
