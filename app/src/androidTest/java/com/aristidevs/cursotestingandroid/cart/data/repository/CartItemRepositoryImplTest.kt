package com.aristidevs.cursotestingandroid.cart.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartItemRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var cartItemRepository: CartItemRepository

    @Before
    fun setUp() =
        runTest {
            hilt.inject()

            cartItemRepository.clearCart()

            val productsJson =
                """
                {"products":[
                    {"id":"cp1","name":"Pan","priceCents":100,"category":"Comida","stock":10},
                    {"id":"cp2","name":"Leche","priceCents":200,"category":"Comida","stock":2}
                ]}
                """.trimIndent()

            mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenEmptyCart_whenGetCartItems_thenEmitsEmptyList() =
        runTest {
            val items = cartItemRepository.getCartItems().first()
            assertTrue(items.isEmpty())
        }

    @Test
    fun givenEmptyCart_whenAddToCart_thenGetCartItemsContainsItem() =
        runTest {
            cartItemRepository.addToCart("id1", 2)

            val items = cartItemRepository.getCartItems().first()

            assertEquals(1, items.size)
            assertEquals("id1", items.first().productId)
            assertEquals(2, items.first().quantity)
        }

    @Test
    fun givenExistingItem_whenAddToCartAgain_thenQuantityIsMerged() =
        runTest {
            cartItemRepository.addToCart("id1", 1)
            cartItemRepository.addToCart("id1", 2)

            val items = cartItemRepository.getCartItems().first()

            assertEquals(1, items.size)
            assertEquals(3, items.first().quantity)
        }

    @Test(expected = AppError.NotFoundError::class)
    fun givenEmptyCart_whenRemoveFromCart_thenThrowsNotFound() =
        runTest {
            cartItemRepository.removeFromCart("error")
        }

    @Test
    fun givenItemInCart_whenUpdateQuantity_thenQuantityMustBeUpdated() =
        runTest {
            cartItemRepository.addToCart("id1", 1)
            cartItemRepository.updateQuantity("id1", 5)

            val items = cartItemRepository.getCartItems().first()

            assertEquals(5, items.first().quantity)
        }

    @Test
    fun givenItemInCart_whenRemoveFromCart_thenCartIsEmpty() =
        runTest {
            cartItemRepository.addToCart("id1", 1)
            cartItemRepository.removeFromCart("id1")

            val items = cartItemRepository.getCartItems().first()
            assertTrue(items.isEmpty())
        }

    @Test
    fun givenMultipleItems_whenClearCart_thenCartIsEmpty() =
        runTest {
            cartItemRepository.addToCart("id1", 1)
            cartItemRepository.addToCart("id2", 1)
            cartItemRepository.clearCart()

            val items = cartItemRepository.getCartItems().first()
            assertTrue(items.isEmpty())
        }

    @Test
    fun givenItemInCart_whenGetCartItemById_thenRetunsItem() =
        runTest {
            cartItemRepository.addToCart("id1", 1)

            val item = cartItemRepository.getCartItemById("id1")

            assertTrue(item != null)
            assertTrue(item!!.productId == "id1")
            assertEquals(1, item.quantity)
        }
}
