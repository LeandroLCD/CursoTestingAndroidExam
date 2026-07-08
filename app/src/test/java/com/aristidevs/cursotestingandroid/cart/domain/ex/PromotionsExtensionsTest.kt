package com.aristidevs.cursotestingandroid.cart.domain.ex

import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.Promotion
import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.Instant

class PromotionsExtensionsTest {
    private val now = Instant.parse("2026-04-03T10:00:00Z")

    @Test
    fun givenFuturePromotion_whenActiveAt_thenExclude() {
        // Given
        val futurePromotion =
            promotion {
                withStartTime(now.plusSeconds(10))
                withEndTime(now.plusSeconds(100))
            }
        val promotions = listOf(futurePromotion)

        // When
        val result = promotions.activeAt(now)

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun givenExpiredPromotion_whenActiveAt_thenExclude() {
        // Given
        val expiredPromotion =
            promotion {
                withStartTime(now.minusSeconds(100))
                withEndTime(now.minusSeconds(10))
            }
        val promotions = listOf(expiredPromotion)

        // When
        val result = promotions.activeAt(now)

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun givenOnGoingPromotion_whenActiveAt_thenInclude() {
        // Given
        val activePromotion =
            promotion {
                withStartTime(now.minusSeconds(1))
                withEndTime(now.plusSeconds(1))
            }
        val promotions = listOf(activePromotion)

        // When
        val result = promotions.activeAt(now)

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun givenExactStartTimePromotion_whenActiveAt_thenInclude() {
        // Given
        val activePromotion =
            promotion {
                withStartTime(now)
                withEndTime(now.plusSeconds(100))
            }
        val promotions = listOf(activePromotion)

        // When
        val result = promotions.activeAt(now)

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun givenExactEndTimePromotion_whenActiveAt_thenInclude() {
        // Given
        val activePromotion =
            promotion {
                withStartTime(now.minusSeconds(100))
                withEndTime(now)
            }
        val promotions = listOf(activePromotion)

        // When
        val result = promotions.activeAt(now)

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun givenEmptyList_whenActiveAt_thenReturnEmpty() {
        // Given
        val promotions = emptyList<Promotion>()

        // When
        val result = promotions.activeAt(now)

        // Then
        assertEquals(0, result.size)
    }
}
