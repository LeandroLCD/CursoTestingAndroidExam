package com.aristidevs.cursotestingandroid.core.utils

import android.util.Log
import app.cash.turbine.ReceiveTurbine

suspend fun <T> ReceiveTurbine<T>.awaitStateMatching(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
