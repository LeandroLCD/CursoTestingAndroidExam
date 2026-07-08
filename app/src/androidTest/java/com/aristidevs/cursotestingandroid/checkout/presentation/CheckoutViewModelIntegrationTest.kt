package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * EXAMEN — Tests de INTEGRACIÓN del ViewModel de checkout (extremo a extremo).
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] con casos de uso reales + Room + MockWebServer.
 * Pistas: inyecta dependencias con Hilt, prepara el carrito real, observa `uiState` con Turbine,
 * y verifica que tras un pedido OK el estado es Success y el carrito queda vacío.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CheckoutViewModelIntegrationTest {

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenItemsInCart_whenViewModelInitialized_thenIdleStateWithSummary() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenValidFormAndSuccessfulOrder_whenOnConfirm_thenSuccessStateAndCartCleared() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenErrorState() {
        // GIVEN

        // WHEN

        // THEN
    }
}
