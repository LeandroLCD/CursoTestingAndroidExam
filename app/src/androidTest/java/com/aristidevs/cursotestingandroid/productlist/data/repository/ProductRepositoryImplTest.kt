package com.aristidevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        hilt.inject()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    private val productsJson =
        """
        {"products":[
            {"id":"p1","name":"Pan","description":"Pan fresco","priceCents":150,"category":"Comida","stock":10},
            {"id":"p2","name":"Leche","description":"Leche entera","priceCents":200,"category":"Lácteos","stock":5}
        ]}
        """.trimIndent()

    @Test
    fun givenValidProductsJson_whenRefreshIsCalled_thenDatabaseEmitProductsFromRoom() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

            productRepository.refreshProduct()

            val products = productRepository.getProducts().first()

            assertTrue(products.isNotEmpty())
            assertTrue(products.size == 2)
            assertEquals("Pan", products.find { it.id == "p1" }?.name)
        }

    @Test
    fun givenEmptyProductsJson_whenRefreshIsCalled_thenGetProductsEmitsEmptyList() =
        runTest {
            mockWebServer.server.enqueue(
                MockResponse().setBody("""{"products":[]}""").setResponseCode(200),
            )

            productRepository.refreshProduct()

            val products = productRepository.getProducts().first()

            assertTrue(products.isEmpty())
        }

    @Test
    fun givenProductsJson_whenRefreshAndGetProductById_thenReturnsCorrectProduct() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

            productRepository.refreshProduct()

            val product = productRepository.getProductById("p1").first()
            assertNotNull(product)
            assertEquals("Pan", product?.name)
        }

    @Test(expected = Exception::class)
    fun givenServerReturns500_whenRefreshIsCalled_thenItThrowsException() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setResponseCode(500))

            productRepository.refreshProduct()
        }

    @Test
    fun givenCachedProducts_whenRefreshWithNewProducts_thenFlowEmitsUpdatedData() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
            productRepository.refreshProduct()

            val productsJsonUpdated =
                """
                {"products":[
                    {"id":"p1","name":"Pan integral","description":"Pan fresco","priceCents":450,"category":"Comida","stock":10}
                ]}
                """.trimIndent()

            mockWebServer.server.enqueue(
                MockResponse().setBody(productsJsonUpdated).setResponseCode(200),
            )
            productRepository.refreshProduct()

            val products = productRepository.getProducts().first()

            assertEquals("Pan integral", products.find { it.id == "p1" }?.name)
            assertEquals(4.5, products.find { it.id == "p1" }?.price)
        }

    @Test
    fun givenProductsEndpoint_whenRefreshIsCalled_thenRequestIsGetToCorrectPath() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
            productRepository.refreshProduct()

            val request = mockWebServer.server.takeRequest()
            assertEquals("GET", request.method)
            assertTrue(request.path?.contains("data/products.json") == true)
        }
}
