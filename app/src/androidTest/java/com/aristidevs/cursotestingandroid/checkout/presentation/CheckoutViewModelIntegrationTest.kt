package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.aristidevs.cursotestingandroid.core.utils.awaitStateMatching
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

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

    @Inject
    lateinit var cartRepository: CartItemRepository

    @Inject
    lateinit var placeOrderUseCase: PlaceOrderUseCase

    @Inject
    lateinit var getCartSummaryUseCase: GetCartSummaryUseCase

    val viewModel by lazy {
        CheckoutViewModel(
            placeOrderUseCase = placeOrderUseCase,
            getCartSummaryUseCase = getCartSummaryUseCase,
        )
    }

    @Before
    fun setUp() {
        hilt.inject()
    }

    /**
     * Dados items ya en el carrito, cuando se inicializa el [CheckoutViewModel],
     * entonces el primer estado emitido debe ser Idle y debe mostrar el resumen del carrito.
     */
    @Test
    fun givenItemsInCart_whenViewModelInitialized_thenIdleStateWithSummary() = runTest {
        // GIVEN
        val product = coffee()
        val quantity = 2
        cartRepository.addToCart(product.id, quantity)

        // WHEN
        viewModel.uiState.test {
            val item = awaitStateMatching { it is CheckoutUiState.Idle } as? CheckoutUiState.Idle

            // THEN
            assertNotNull(item)
            assertNotEquals(item.summary.subtotal, product.price * quantity)
        }
    }

    /**
     * Dado un formulario válido y un pedido exitoso (200 desde MockWebServer),
     * cuando el usuario confirma con [CheckoutViewModel.onConfirm], entonces el estado
     * terminal debe ser Success y el carrito real (Room) debe quedar vacío.
     */
    @Test
    fun givenValidFormAndSuccessfulOrder_whenOnConfirm_thenSuccessStateAndCartCleared() {
        // GIVEN

        // WHEN

        // THEN
    }

    /**
     * Dado que el endpoint de pedido responde con error (5xx o red caída en MockWebServer),
     * cuando se llama a [CheckoutViewModel.onConfirm], entonces el estado terminal debe ser
     * Error y el carrito NO debe vaciarse.
     */
    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenErrorState() {
        // GIVEN

        // WHEN

        // THEN
    }
}
