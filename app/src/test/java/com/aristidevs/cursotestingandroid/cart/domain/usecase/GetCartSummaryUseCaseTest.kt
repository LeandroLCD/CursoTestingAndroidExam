package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.model.PromotionType
import com.aristidevs.cursotestingandroid.productlist.domain.model.PromotionType.PERCENT
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetCartSummaryUseCaseTest {
    private lateinit var clock: FakeSystemClock
    private lateinit var cartRepository: FakeCartItemRepository
    private lateinit var productRepository: FakeProductRepository
    private lateinit var promoRepository: FakePromotionRepository

    @Before
    fun setUp() {
        clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }
        cartRepository = FakeCartItemRepository()
        productRepository = FakeProductRepository()
        promoRepository = FakePromotionRepository()
    }

    private fun useCase() =
        GetCartSummaryUseCase(
            cartRepository,
            productRepository,
            promoRepository,
            GetPromotionForProduct(),
            clock,
        )

    @Test
    fun `given percent promotion when invoke then calculate correctly`() =
        runTest {
            val productId = "p1"
            val product =
                product {
                    withId(productId)
                    withPrice(100.0)
                }
            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withType(PERCENT)
                    withValue(10.0)
                    withStartTime(clock.now().minusSeconds(10))
                    withEndTime(clock.now().plusSeconds(10))
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(2)
                }

            productRepository.setProducts(listOf(product))
            promoRepository.setPromotions(listOf(promo))
            cartRepository.setCartItems(listOf(cartItem))

            val summary = (useCase()()).first()

            assertEquals(180.0, summary.finalTotal)
            assertEquals(20.0, summary.discountTotal)
            assertEquals(200.0, summary.subtotal)
        }

    @Test
    fun `given 3 items in 2x1 promotion when invoke then only discounts 1 unit`() =
        runTest {
            val productId = "p1"
            val product =
                product {
                    withId(productId)
                    withPrice(100.0)
                }
            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withType(PromotionType.BUY_X_PAY_Y)
                    withBuyQuantity(2)
                    withValue(1.0)
                    withStartTime(clock.now().minusSeconds(10))
                    withEndTime(clock.now().plusSeconds(10))
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(3)
                }

            productRepository.setProducts(listOf(product))
            promoRepository.setPromotions(listOf(promo))
            cartRepository.setCartItems(listOf(cartItem))

            val summary = (useCase()()).first()

            assertEquals(300.0, summary.subtotal)
            assertEquals(200.0, summary.finalTotal)
            assertEquals(100.0, summary.discountTotal)
        }

    @Test
    fun `given multiple products with different promotions when invoke them sums all correctly`() =
        runTest {
            val now = clock.now()
            val p1 =
                product {
                    withId("p1")
                    withPrice(100.0)
                } // Con promo
            val p2 =
                product {
                    withId("p2")
                    withPrice(50.0)
                } // SIN promo

            val promoPercent =
                promotion {
                    withProductIds(listOf("p1"))
                    withType(PERCENT)
                    withValue(10.0)
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(10))
                }

            val cart =
                listOf(
                    cartItem {
                        withProductId("p1")
                        withQuantity(1)
                    },
                    cartItem {
                        withProductId("p2")
                        withQuantity(1)
                    },
                )

            productRepository.setProducts(listOf(p1, p2))
            promoRepository.setPromotions(listOf(promoPercent))
            cartRepository.setCartItems(cart)

            val summary = useCase()().first()

            assertEquals(150.0, summary.subtotal)
            assertEquals(140.0, summary.finalTotal)
            assertEquals(10.0, summary.discountTotal)
        }

    @Test
    fun `given expired promotion when invoke then discount is zero`() =
        runTest {
            val now = clock.now()
            val p1 =
                product {
                    withId("p1")
                    withPrice(100.0)
                }

            val promoPercent =
                promotion {
                    withProductIds(listOf("p1"))
                    withType(PERCENT)
                    withValue(10.0)
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.minusSeconds(5))
                }

            productRepository.setProducts(listOf(p1))
            promoRepository.setPromotions(listOf(promoPercent))
            cartRepository.setCartItems(
                listOf(
                    cartItem {
                        withProductId("p1")
                        withQuantity(1)
                    },
                ),
            )

            val summary = useCase()().first()

            assertEquals(0.0, summary.discountTotal)
            assertEquals(100.0, summary.finalTotal)
        }

    @Test
    fun `given active promotion when time advances then summary update automatically`() =
        runTest {
            val now = clock.now()
            val p1 =
                product {
                    withId("p1")
                    withPrice(100.0)
                }

            val promoPercent =
                promotion {
                    withProductIds(listOf("p1"))
                    withType(PERCENT)
                    withValue(10.0)
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(5))
                }

            productRepository.setProducts(listOf(p1))
            promoRepository.setPromotions(listOf(promoPercent))
            cartRepository.setCartItems(
                listOf(
                    cartItem {
                        withProductId("p1")
                        withQuantity(1)
                    },
                ),
            )

            val summaryFlow = useCase()()

            assertEquals(10.0, summaryFlow.first().discountTotal)

            clock.advanceTime(6)

            assertEquals(0.0, summaryFlow.first().discountTotal)
        }
}
