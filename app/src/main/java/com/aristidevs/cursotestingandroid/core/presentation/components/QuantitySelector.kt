package com.aristidevs.cursotestingandroid.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    modifier: Modifier = Modifier,
    quantity: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecreaseSelected: () -> Unit,
    onIncreaseSelected: () -> Unit,
    increaseTestTag: String? = null,
    decreaseTestTag: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onDecreaseSelected() },
            modifier =
                Modifier
                    .size(36.dp)
                    .then(decreaseTestTag?.let { Modifier.testTag(it) } ?: Modifier),
            enabled = canDecrease,
        ) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier.size(20.dp))
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        IconButton(
            onClick = { onIncreaseSelected() },
            modifier =
                Modifier
                    .size(36.dp)
                    .then(increaseTestTag?.let { Modifier.testTag(it) } ?: Modifier),
            enabled = canIncrease,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier.size(20.dp))
        }
    }
}
