package com.aristidevs.cursotestingandroid.checkout.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    @Test
    fun givenSuccessfulResponse_whenPlaceOrder_thenReturnsOrderConfirmation() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun given404Response_whenPlaceOrder_thenThrowsNotFoundError() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenNetworkFailure_whenPlaceOrder_thenThrowsNetworkError() {
        // GIVEN

        // WHEN

        // THEN
    }
}
