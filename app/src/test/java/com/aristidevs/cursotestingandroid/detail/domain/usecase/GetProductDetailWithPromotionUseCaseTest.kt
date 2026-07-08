package com.aristidevs.cursotestingandroid.detail.domain.usecase

import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetProductDetailWithPromotionUseCaseTest {
    private lateinit var clock: FakeSystemClock
    private lateinit var productRepository: FakeProductRepository
    private lateinit var promotionRepository: FakePromotionRepository

    @Before
    fun setUp() {
        clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }
        promotionRepository = FakePromotionRepository()
        productRepository = FakeProductRepository()
    }

    private fun useCase() =
        GetProductDetailWithPromotionUseCase(
            productRepository,
            promotionRepository,
            GetPromotionForProduct(),
            clock,
        )

    @Test
    fun `given active promotion when invoke then returns product with promotion`() =
        runTest {
            // Given
            val productId = "p1"
            val p =
                product {
                    withId(productId)
                    withName("juan")
                }
            val now = clock.now()

            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(10))
                }

            productRepository.setProducts(listOf(p))
            promotionRepository.setPromotions(listOf(promo))

            // WHEN
            val result = useCase()(productId).first()

            // THEN
            assertNotNull(result)
            assertNotNull(result?.promotion)
            assertEquals(productId, result?.product?.id)
        }

    @Test
    fun `given expired promotion when invoke then returns product without promotion`() =
        runTest {
            // Given
            val productId = "p1"
            val p =
                product {
                    withId(productId)
                    withName("juan")
                }
            val now = clock.now()

            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.minusSeconds(10))
                }

            productRepository.setProducts(listOf(p))
            promotionRepository.setPromotions(listOf(promo))

            // WHEN
            val result = useCase()(productId).first()

            // THEN
            assertNotNull(result?.product)
            assertNull(result?.promotion)
        }

    @Test
    fun `given non existing product id when invokes then returns null`() =
        runTest {
            productRepository.setProducts(emptyList())

            val result = useCase()("leandro").first()

            assertNull(result)
        }

    @Test
    fun `given active promotion when time advances then product promotion becomes null`() =
        runTest {
            // Given
            val productId = "p1"
            val p =
                product {
                    withId(productId)
                    withName("jujo")
                }
            val now = clock.now()

            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(5))
                }

            productRepository.setProducts(listOf(p))
            promotionRepository.setPromotions(listOf(promo))

            // WHEN
            val result = useCase()(productId)

            assertNotNull(result.first()?.promotion)

            clock.advanceTime(6)

            assertNull(result.first()?.promotion)
        }
}
