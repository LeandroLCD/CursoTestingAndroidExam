package com.aristidevs.cursotestingandroid.checkout.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.aristidevs.cursotestingandroid.core.builders.OrderConfirmationBuilder
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * EXAMEN — Tests de INTEGRACIÓN del repositorio de pedidos.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [OrderRepositoryImpl] sobre RemoteDataSource real + MockWebServer
 * (200 -> OrderConfirmation mapeada; 404 -> AppError.NotFoundError; red caída -> AppError.NetworkError).
 * Pistas: encola respuestas en `mockWebServer.server`, inyecta con Hilt (`hilt.inject()`).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OrderRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    lateinit var repository: OrderRepository

    @Inject
    lateinit var remoteDataSource: RemoteDataSource

    @Before
    fun setUp() {
        hilt.inject()
        repository = OrderRepositoryImpl(remoteDataSource)
    }

    @Test
    fun givenSuccessfulResponse_whenPlaceOrder_thenReturnsOrderConfirmation() = runTest {
        // GIVEN
        val id = "test01"
        mockWebServer.server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                OrderConfirmationBuilder().withOrderId(id).buildJson(),
            ),
        )

        // WHEN
        val result = runCatching { repository.placeOrder() }

        // THEN
        assert(result.isSuccess)
        assertEquals(id, result.getOrThrow().orderId)
    }

    @Test
    fun given404Response_whenPlaceOrder_thenThrowsNotFoundError() = runTest {
        // GIVEN
        mockWebServer.server.enqueue(
            MockResponse().setResponseCode(404),
        )

        // WHEN
        val result = runCatching { repository.placeOrder() }

        // THEN
        assert(result.isFailure)
        result.onFailure {
            assertEquals(AppError.NotFoundError, it)
        }
    }

    @Test
    fun givenNetworkFailure_whenPlaceOrder_thenThrowsNetworkError() = runTest{
        // GIVEN
        mockWebServer.server.enqueue(
            MockResponse().setResponseCode(504),
        )

        // WHEN
        val result = runCatching { repository.placeOrder() }

        // THEN
        assert(result.isFailure)
        result.onFailure {
            assertEquals(AppError.NetworkError, it)
        }
    }
}
