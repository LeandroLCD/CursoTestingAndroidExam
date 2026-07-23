package com.aristidevs.cursotestingandroid.checkout.presentation

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.data.repository.fake.FakeOrderRepository
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.orderConfirmation
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.core.utils.awaitStateMatching
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del ViewModel de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] — estados [CheckoutUiState], `canSubmit`, `onConfirm`, eventos.
 * Pistas: usa Turbine sobre `uiState`/`event`, `runTest(mainDispatcherRule.scheduler)`,
 * fakes (FakeCartItemRepository, FakeProductRepository, FakePromotionRepository, FakeSystemClock)
 * y un fake de OrderRepository que tendrás que crear.
 */
class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    lateinit var viewModel: CheckoutViewModel
    private val orderRepository = FakeOrderRepository()

    private val cartItemRepository = FakeCartItemRepository()

    private val productRepository = FakeProductRepository()

    private val promotionRepository = FakePromotionRepository()
    private val clock = FakeSystemClock()

    fun createViewModel() {
        viewModel = CheckoutViewModel(
            placeOrderUseCase = PlaceOrderUseCase(
                orderRepository = orderRepository,
                cartItemRepository = cartItemRepository,
            ),
            getCartSummaryUseCase = GetCartSummaryUseCase(
                cartItemRepository = cartItemRepository,
                productRepository = productRepository,
                promotionRepository = promotionRepository,
                getPromotionForProduct = GetPromotionForProduct(),
                clock = clock,
            ),
        )
    }
    @Before
    fun setUp() {
        createViewModel()
    }

    @Test
    fun `given empty cart when initialized then canSubmit is false`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        cartItemRepository.setCartItems(emptyList())

        // WHEN
        viewModel.uiState.test {
            // THEN
            val state = awaitItem() as CheckoutUiState.Idle
            assertFalse(state.canSubmit)
            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `given valid form and non empty cart when form completed then canSubmit is true`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val id1 = "product1"
        val id2 = "product2"

        val cartItems = listOf(
            cartItem { withProductId(id1); withQuantity(2) },
            cartItem { withProductId(id2); withQuantity(1) }
        )
        cartItemRepository.setCartItems(cartItems)
        val products = listOf(
            product { withId(id1); withPrice(10.0) },
            product { withId(id2); withPrice(20.0) }
        )
        productRepository.setProducts(products)
        viewModel.onNameChange("John Doe")
        viewModel.onAddressChange("123 Main St")
        viewModel.onEmailChange("test@correo.cl")



        // WHEN
        viewModel.uiState.test {
            // THEN
            val state =  awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle
            assertTrue("$state",state.canSubmit)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `given malformed email when email changed then emailError is INVALID_EMAIL and canSubmit is false`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val id1 = "product1"
        val id2 = "product2"

        val cartItems = listOf(
            cartItem { withProductId(id1); withQuantity(2) },
            cartItem { withProductId(id2); withQuantity(1) }
        )
        cartItemRepository.setCartItems(cartItems)
        val products = listOf(
            product { withId(id1); withPrice(10.0) },
            product { withId(id2); withPrice(20.0) }
        )
        productRepository.setProducts(products)
        viewModel.onNameChange("John Doe")
        viewModel.onAddressChange("123 Main St")
        viewModel.onEmailChange("test@correo")



        // WHEN
        viewModel.uiState.test {
            // THEN
            val state =  awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle
            assertTrue("$state",state.errors.emailError == FieldError.INVALID_EMAIL)
            assertFalse("$state",state.canSubmit)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `given valid form when onConfirm succeeds then emits Success state`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val id1 = "product1"
        val id2 = "product2"

        val cartItems = listOf(
            cartItem { withProductId(id1); withQuantity(2) },
            cartItem { withProductId(id2); withQuantity(1) }
        )
        cartItemRepository.setCartItems(cartItems)
        val products = listOf(
            product { withId(id1); withPrice(10.0) },
            product { withId(id2); withPrice(20.0) }
        )
        productRepository.setProducts(products)
        viewModel.onNameChange("John Doe")
        viewModel.onAddressChange("123 Main St")
        viewModel.onEmailChange("test@correo.cl")
        orderRepository.setOrderConfirmation(
            orderConfirmation {
                withOrderId("12345")
            }
        )




        // WHEN
        viewModel.uiState.test {
            val idle = awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle
            assertTrue(idle.canSubmit)

            // THEN
            viewModel.onConfirm()
            val state =  awaitStateMatching { it is CheckoutUiState.Success } as CheckoutUiState.Success
            assertTrue("$state",state.confirmation.orderId.isNotEmpty())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `given place order fails when onConfirm then emits Error state and ShowMessage event`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val id1 = "product1"
        val id2 = "product2"

        val cartItems = listOf(
            cartItem { withProductId(id1); withQuantity(2) },
            cartItem { withProductId(id2); withQuantity(1) }
        )
        cartItemRepository.setCartItems(cartItems)
        val products = listOf(
            product { withId(id1); withPrice(10.0) },
            product { withId(id2); withPrice(20.0) }
        )
        productRepository.setProducts(products)
        viewModel.onNameChange("John Doe")
        viewModel.onAddressChange("123 Main St")
        viewModel.onEmailChange("test@correo.cl")

        val throwable = Exception("Error John Doe")
        orderRepository.setThrowable(throwable)

        // WHEN
        viewModel.event.test {
            viewModel.uiState.test {
                // THEN
                viewModel.onConfirm()

                val state = awaitStateMatching { it is CheckoutUiState.Error } as CheckoutUiState.Error
                assertTrue("$state", state.message == throwable.message)
                cancelAndConsumeRemainingEvents()
            }

            val showMessage = awaitStateMatching { it is CheckoutEvent.ShowMessage } as CheckoutEvent.ShowMessage
            assertTrue("$showMessage", showMessage.message == throwable.message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `given invalid form when onConfirm then does not place order`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val id1 = "product1"
        val id2 = "product2"

        val cartItems = listOf(
            cartItem { withProductId(id1); withQuantity(2) },
            cartItem { withProductId(id2); withQuantity(1) }
        )
        cartItemRepository.setCartItems(cartItems)
        val products = listOf(
            product { withId(id1); withPrice(10.0) },
            product { withId(id2); withPrice(20.0) }
        )
        productRepository.setProducts(products)
        viewModel.onNameChange("John Doe")
        viewModel.onAddressChange("123 Main St")
        viewModel.onEmailChange("test@correo")


        // WHEN
        viewModel.uiState.test {
            val idle = awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle
            assertFalse(idle.canSubmit)
            assertEquals(FieldError.INVALID_EMAIL, idle.errors.emailError)

            // THEN
            viewModel.onConfirm()

            assertEquals(0, orderRepository.invocationCount)
            cancelAndConsumeRemainingEvents()
        }
    }

}
