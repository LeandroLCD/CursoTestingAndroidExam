package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aristidevs.cursotestingandroid.cart.presentation.CartUiState
import com.aristidevs.cursotestingandroid.cart.presentation.CartViewModel
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_LIST_LIST
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_LIST_LOADING
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.aristidevs.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.aristidevs.cursotestingandroid.productlist.presentation.components.ProductItem

@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
    cartViewMode: CartViewModel = hiltViewModel(),
    navigateToSettings: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToCart: () -> Unit,
) {
    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val cartUiState by cartViewMode.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val filterVisible by productListViewModel.filterVisible.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        productListViewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val cartItemCount =
        remember(cartUiState) {
            when (val state = cartUiState) {
                is CartUiState.Success -> {
                    state.cartItems.sumOf { it.cartItem.quantity }
                }

                else -> 0
            }
        }

    ProductListContent(
        uiState = uiState,
        cartItemCount = cartItemCount,
        filterVisible = filterVisible,
        snackbarHostState = snackbarHostState,
        onFilterSelected = { showFilters -> productListViewModel.setFilterVisible(showFilters) },
        onSettingsSelected = navigateToSettings,
        onCartSelected = navigateToCart,
        onCategorySelected = { category -> productListViewModel.setCategory(category) },
        onSortOptionSelected = { sortOption -> productListViewModel.setSortOption(sortOption) },
        onProductSelected = { productWithPromotion -> navigateToProductDetail(productWithPromotion.product.id) },
    )
}

@Composable
fun ProductListContent(
    uiState: ProductListUiState,
    cartItemCount: Int,
    filterVisible: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onFilterSelected: (Boolean) -> Unit,
    onSettingsSelected: () -> Unit,
    onCartSelected: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onProductSelected: (ProductWithPromotion) -> Unit,
) {
    Scaffold(topBar = {
        HomeTopAppBar(
            filtersVisible = filterVisible,
            cartItemCount = cartItemCount,
            onFiltersSelected = onFilterSelected,
            onSettingsSelected = onSettingsSelected,
            onCartSelected = onCartSelected,
        )
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValue ->
        when (uiState) {
            is ProductListUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag(PRODUCT_LIST_LOADING))
                }
            }

            is ProductListUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ERROR",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is ProductListUiState.Success -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                ) {
                    AnimatedVisibility(
                        visible = filterVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        FiltersMenu(
                            state = uiState,
                            onCategorySelected = onCategorySelected,
                            onSortSelected = onSortOptionSelected,
                        )
                    }

                    Text(
                        "${uiState.products.size} productos",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    if (uiState.products.isEmpty()) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("🔍", style = MaterialTheme.typography.displayMedium)
                                Text(
                                    "No se encontraron productos",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.testTag(PRODUCT_LIST_LIST)) {
                            items(uiState.products) { item: ProductWithPromotion ->
                                ProductItem(
                                    item = item,
                                    onClick = onProductSelected,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
