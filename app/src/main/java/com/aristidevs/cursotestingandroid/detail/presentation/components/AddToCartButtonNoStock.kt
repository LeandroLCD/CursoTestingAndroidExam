package com.aristidevs.cursotestingandroid.detail.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_BUTTON_NO_STOCK

@Composable
fun AddToCartButtonNoStock(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Box(Modifier.padding(16.dp)) {
            Button(
                modifier = Modifier.fillMaxWidth().testTag(PRODUCT_DETAIL_BUTTON_NO_STOCK),
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sin stock disponible",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
