package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.builders.promotion
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class GetCartItemsWithPromotionsUseCaseTest {
    private val clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }

    private fun useCase(
        cart: FakeCartItemRepository = FakeCartItemRepository(),
        products: FakeProductRepository = FakeProductRepository(),
        promos: FakePromotionRepository = FakePromotionRepository(),
        clock: FakeSystemClock = this.clock,
    ) = GetCartItemsWithPromotionsUseCase(
        cart,
        products,
        promos,
        GetPromotionForProduct(),
        clock,
    )

    @Test
    fun `given empty cart when invokes then returns empty list`() =
        runTest {
            val cart = FakeCartItemRepository().apply { setCartItems(emptyList()) }

            val result = (useCase(cart = cart)()).first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `given existing cart item with active promotion when invoke then returns item with promotion`() =
        runTest {
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val now = clock.now()
            val promo =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(10))
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(2)
                }

            val cart = FakeCartItemRepository().apply { setCartItems(listOf(cartItem)) }
            val products = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promotions = FakePromotionRepository().apply { setPromotions(listOf(promo)) }

            val result = useCase(cart = cart, products = products, promos = promotions)().first()

            assertEquals(1, result.size)
            assertNotNull(result.first().item.promotion)
        }

    @Test
    fun `given cart item without matching product when invoke then skip item`() =
        runTest {
            val cart =
                FakeCartItemRepository().apply {
                    setCartItems(listOf(cartItem { withProductId("ghost-id") }))
                }
            val products =
                FakeProductRepository().apply {
                    setProducts(listOf(product { withId("other-id") }))
                }

            val result = useCase(products = products, cart = cart)().first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `given promotion ending exactly now when invoke then it must be include`() =
        runTest {
            val now = clock.now()
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val endingPromotion =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now)
                }

            val cart =
                FakeCartItemRepository().apply { setCartItems(listOf(cartItem { withProductId(productId) })) }
            val products = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promotions = FakePromotionRepository().apply { setPromotions(listOf(endingPromotion)) }

            val result = useCase(cart = cart, promos = promotions, products = products)().first()

            assertNotNull(result.first().item.promotion)
        }

    @Test
    fun `given expired promotion when invoke then item remains but without promotion`() =
        runTest {
            val now = clock.now()
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val endPromotion =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now.minusSeconds(1))
                }

            val cart =
                FakeCartItemRepository().apply { setCartItems(listOf(cartItem { withProductId(productId) })) }
            val products = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promotions = FakePromotionRepository().apply { setPromotions(listOf(endPromotion)) }

            val result = useCase(cart = cart, promos = promotions, products = products)().first()

            assertNull(result.first().item.promotion)
        }

    @Test
    fun `given active promotion when time advances then flow emits update list without promotion`() =
        runTest {
            val now = clock.now()
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val promotion =
                promotion {
                    withProductIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now.plusSeconds(5))
                }

            val cart =
                FakeCartItemRepository().apply { setCartItems(listOf(cartItem { withProductId(productId) })) }
            val products = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promotions = FakePromotionRepository().apply { setPromotions(listOf(promotion)) }

            val myUseCase = useCase(cart = cart, promos = promotions, products = products)()

            val firstEmission = myUseCase.first()
            assertNotNull(firstEmission.first().item.promotion)

            clock.advanceTime(6)

            val secondEmission = myUseCase.first()
            assertNull(secondEmission.first().item.promotion)
        }
}
