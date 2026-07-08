package com.aristidevs.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_BADGE
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_CART
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_FILTER
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_SETTINGS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    filtersVisible: Boolean = true,
    cartItemCount: Int,
    onFiltersSelected: (Boolean) -> Unit,
    onSettingsSelected: () -> Unit,
    onCartSelected: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                "MiniMarket",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = Bold,
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        actions = {
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_FILTER),
                onClick = { onFiltersSelected(!filtersVisible) },
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = if (filtersVisible) "Ocultar filtros" else "Mostrar filtros",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_SETTINGS),
                onClick = { onSettingsSelected() },
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            BadgedBox(modifier = Modifier.padding(end = 4.dp), badge = {
                if (cartItemCount > 0) {
                    Badge(modifier = Modifier.testTag(TOP_APP_BAR_BADGE)) {
                        Text(
                            if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = Bold,
                        )
                    }
                }
            }) {
                IconButton(
                    modifier = Modifier.testTag(TOP_APP_BAR_CART),
                    onClick = { onCartSelected() },
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
    )
}
