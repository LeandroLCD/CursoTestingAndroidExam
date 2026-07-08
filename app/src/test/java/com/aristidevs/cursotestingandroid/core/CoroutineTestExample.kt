package com.aristidevs.cursotestingandroid.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CoroutineTestExample {
    private suspend fun coroutinesSum(
        a: Int,
        b: Int,
    ): Int {
        delay(145000)
        return a + b
    }

    @Test
    fun coroutinesSum_returnsCorrectSum() =
        runTest {
            val result = coroutinesSum(2, 2)
            assert(result == 4)
        }
}
