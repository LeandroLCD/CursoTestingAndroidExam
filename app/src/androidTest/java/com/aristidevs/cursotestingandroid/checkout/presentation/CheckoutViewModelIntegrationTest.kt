package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.OrderErrorDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.asAsset
import com.aristidevs.cursotestingandroid.core.utils.awaitStateMatching
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertFalse
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
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

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
    fun setUp() = runTest {
        mockWebServer.server.dispatcher = MiniMarketApiDispatcher(
            productJson = "product_list_default.json".asAsset(),
        )
        hilt.inject()
        cartRepository.clearCart()
        productRepository.refreshProduct()
        promotionRepository.refreshPromotions()
    }

    /**
     * Dados items ya en el carrito, cuando se inicializa el [CheckoutViewModel],
     * entonces el primer estado emitido debe ser Idle y debe mostrar el resumen del carrito.
     */
    @Test
    fun givenItemsInCart_whenViewModelInitialized_thenIdleStateWithSummary() = runTest {
        // GIVEN
        val productIdOne = "p1"
        val productIdTwo = "p2"
        val quantityOne = 2
        val quantityTwo = 1
        cartRepository.addToCart(productIdOne, quantityOne)
        cartRepository.addToCart(productIdTwo, quantityTwo)
        val expectedSubtotal = 10.0 * quantityOne + 20.0 * quantityTwo

        // WHEN
        viewModel.uiState.test {
            val state = awaitStateMatching { it is CheckoutUiState.Idle } as? CheckoutUiState.Idle

            // THEN
            assertNotNull(state)
            assertEquals(expectedSubtotal, state.summary.subtotal, 0.01)
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
        val productId = "p1"
        val quantity = 2
        cartRepository.addToCart(productId, quantity)
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
        val productId = "p1"
        val quantity = 2
        cartRepository.addToCart(productId, quantity)
        mockWebServer.server.dispatcher = OrderErrorDispatcher()
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("test@mail.cl")
        viewModel.onAddressChange("test address")

        // WHEN
        viewModel.onConfirm()
        viewModel.uiState.test(9.seconds) {
            val state = awaitStateMatching { it is CheckoutUiState.Error }

            val cartItems = cartRepository.getCartItems().firstOrNull()

            // THEN
            assertTrue(state is CheckoutUiState.Error)
            assertNotNull(cartItems)
            assertTrue(cartItems.isNotEmpty())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun givenInvalidForm_whenOnConfirm_thenCanSubmitIsFalse() = runTest {
        // GIVEN
        cartRepository.addToCart("p1", 2)
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("not-an-email")

        // WHEN
        viewModel.uiState.test {
            val idle = awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle

            // THEN
            assertEquals(FieldError.REQUIRED, idle.errors.addressError)
            assertEquals(FieldError.INVALID_EMAIL, idle.errors.emailError)
            assertFalse(idle.canSubmit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun givenInvalidForm_whenOnConfirm_thenCanSubmitIsFalseAndOrderEndpointNotCalled() = runTest {
        // GIVEN
        cartRepository.addToCart("p1", 2)
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("not-an-email")
        val requestsBeforeConfirm = mockWebServer.server.requestCount

        // WHEN
        viewModel.onConfirm()

        // THEN
        assertEquals(requestsBeforeConfirm, mockWebServer.server.requestCount)
        val cartItems = cartRepository.getCartItems().firstOrNull()
        assertNotNull(cartItems)
        assertTrue(cartItems.isNotEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenEmitsShowMessageEvent() = runTest {
        // GIVEN
        cartRepository.addToCart("p1", 2)
        mockWebServer.server.dispatcher = OrderErrorDispatcher()
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("test@mail.cl")
        viewModel.onAddressChange("test address")

        val checkoutEvents = mutableListOf<CheckoutEvent>()
        val collector = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.event.toList(checkoutEvents)
        }

        // WHEN
        viewModel.onConfirm()
        viewModel.uiState.test(6.seconds) {
            awaitStateMatching { it is CheckoutUiState.Error }
            cancelAndIgnoreRemainingEvents()
        }

        // THEN
        assertTrue(checkoutEvents.isNotEmpty())
        assertTrue(checkoutEvents.first() is CheckoutEvent.ShowMessage)
        collector.cancel()
    }

    @Test
    fun givenErrorState_whenOnRetry_thenReturnsToIdleAndCanResubmit() = runTest {
        // GIVEN
        cartRepository.addToCart("p1", 2)
        mockWebServer.server.dispatcher = OrderErrorDispatcher()
        viewModel.onNameChange("test name")
        viewModel.onEmailChange("test@mail.cl")
        viewModel.onAddressChange("test address")

        viewModel.onConfirm()
        viewModel.uiState.test(6.seconds) {
            awaitStateMatching { it is CheckoutUiState.Error }
        }

        // WHEN
        viewModel.onRetry()

        // THEN
        viewModel.uiState.test(3.seconds) {
            val idle = awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle
            assertNotNull(idle)
            assertFalse(idle.isSubmitting)
            assertFalse(idle.isCartEmpty)
            assertTrue(idle.canSubmit)
            assertTrue(idle.errors.isValid)
        }
    }
}
