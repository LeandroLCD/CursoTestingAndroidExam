package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.OrderConfirmationBuilder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.aristidevs.cursotestingandroid.core.utils.awaitStateMatching
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

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
            val state = awaitStateMatching { it is CheckoutUiState.Idle } as? CheckoutUiState.Idle

            // THEN
            assertNotNull(state)
            assertNotEquals(state.summary.subtotal, product.price * quantity)
        }
    }

    /**
     * Dado un formulario válido y un pedido exitoso (200 desde MockWebServer),
     * cuando el usuario confirma con [CheckoutViewModel.onConfirm], entonces el estado
     * terminal debe ser Success y el carrito real (Room) debe quedar vacío.
     */
    @Test
    fun givenValidFormAndSuccessfulOrder_whenOnConfirm_thenSuccessStateAndCartCleared() = runTest {
        // GIVEN
        val product = coffee()
        val quantity = 2
        cartRepository.addToCart(product.id, quantity)
        mockWebServer.server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                OrderConfirmationBuilder().buildJson()
            )
        )
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("test@mail.cl")
        viewModel.onAddressChange("test address")

        // WHEN
        viewModel.onConfirm()
        viewModel.uiState.test(6.seconds) {
            val state = awaitStateMatching { it is CheckoutUiState.Success } as? CheckoutUiState.Success

            val cartItems = cartRepository.getCartItems().firstOrNull()

            // THEN
            assertNotNull(state)
            assertNotNull(cartItems)
            assertTrue(cartItems.isEmpty())
        }
    }

    /**
     * Dado que el endpoint de pedido responde con error (5xx o red caída en MockWebServer),
     * cuando se llama a [CheckoutViewModel.onConfirm], entonces el estado terminal debe ser
     * Error y el carrito NO debe vaciarse.
     */
    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenErrorState() = runTest {
        // GIVEN
        val product = coffee()
        val quantity = 2
        cartRepository.addToCart(product.id, quantity)
        mockWebServer.server.enqueue(
            MockResponse().setResponseCode(500)
        )
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("test@mail.cl")
        viewModel.onAddressChange("test address")

        // WHEN
        viewModel.onConfirm()
        viewModel.uiState.test(6.seconds) {
            val state = awaitStateMatching { it is CheckoutUiState.Error }

            val cartItems = cartRepository.getCartItems().firstOrNull()

            // THEN
            assertTrue(state is CheckoutUiState.Error)
            assertNotNull(cartItems)
            assertTrue(cartItems.isNotEmpty())
        }
    }
}
