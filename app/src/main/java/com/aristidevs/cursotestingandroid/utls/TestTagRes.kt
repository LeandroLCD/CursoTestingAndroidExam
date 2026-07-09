package com.aristidevs.cursotestingandroid.utls

import android.annotation.SuppressLint
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag

@SuppressLint("LocalContextResourcesRead", "LocalContextGetResourceValueCall")
@Composable
fun Modifier.testTagRes(
    @IdRes id: Int,
): Modifier {
    val tag = LocalResources.current.getResourceEntryName(id)
    return testTag(tag)
}

@SuppressLint("LocalContextResourcesRead", "LocalContextGetResourceValueCall")
@Composable
fun Modifier.testTagRes(
    @IdRes id: Int,
    vararg formatArgs: Any?,
    separator: Char = '_',
): Modifier {
    val tag = LocalResources.current.getResourceEntryName(id)
    val tagWithArgs = tag.plus(separator).plus(formatArgs.joinToString(separator = separator.toString()))
    return testTag(tagWithArgs)
}
