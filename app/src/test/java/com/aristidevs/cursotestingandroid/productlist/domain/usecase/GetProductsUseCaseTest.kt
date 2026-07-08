package com.aristidevs.cursotestingandroid.productlist.domain.usecase

import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GetProductsUseCaseTest {
    private fun useCase(
        products: FakeProductRepository = FakeProductRepository(),
        promos: FakePromotionRepository = FakePromotionRepository(),
        settings: FakeSettingsRepository = FakeSettingsRepository(),
        clock: FakeSystemClock = FakeSystemClock(),
    ) = GetProductsUseCase(products, promos, GetPromotionForProduct(), settings, clock)

    @Test
    fun `given promotion ending now when invoke then it should be included`() =
        runTest {
            // GIVEN
            val now = Instant.parse("2026-04-03T10:00:00Z")
            val clock = FakeSystemClock().apply { setTime(now) }

            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                }
            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(60))
                    withEndTime(now)
                }

            val productRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promoRepository = FakePromotionRepository().apply { setPromotions(listOf(promo)) }

            // WHEN
            val result =
                (
                    useCase(
                        products = productRepository,
                        promos = promoRepository,
                        clock = clock,
                    )()
                ).first()

            // THEN
            assertNotNull(result.first())
        }

    @Test
    fun `given active promotion when time advances then promotion should no be longer be returned`() =
        runTest {
            // GIVEN
            val now = Instant.parse("2026-04-03T10:00:00Z")
            val clock = FakeSystemClock().apply { setTime(now) }

            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                }
            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now)
                    withEndTime(now.plusSeconds(5))
                }

            val productRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promoRepository = FakePromotionRepository().apply { setPromotions(listOf(promo)) }

            // when
            val firstResult =
                (
                    useCase(
                        products = productRepository,
                        promos = promoRepository,
                        clock = clock,
                    )()
                ).first()
            clock.advanceTime(6)
            val secondResult =
                (
                    useCase(
                        products = productRepository,
                        promos = promoRepository,
                        clock = clock,
                    )()
                ).first()

            // Then
            assertNotNull(firstResult.first().promotion)
            assertNull(secondResult.first().promotion)
        }

    @Test
    fun `given inStockOnly enabled when product goes out of stock then it should be filtered`() =
        runTest {
            // Given
            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                    withStock(0)
                }
            val settings = FakeSettingsRepository().apply { setInStockOnly(true) }
            val productRepository = FakeProductRepository().apply { setProducts(listOf(product)) }

            val myUseCase = useCase(settings = settings, products = productRepository)

            // when
            val result = myUseCase().first()

            // then
            assertTrue(result.isEmpty())
        }
}
