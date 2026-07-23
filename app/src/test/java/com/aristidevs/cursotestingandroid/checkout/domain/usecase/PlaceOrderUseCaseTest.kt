package com.aristidevs.cursotestingandroid.checkout.domain.usecase

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.checkout.data.repository.fake.FakeOrderRepository
import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.orderConfirmation
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del caso de uso de realizar pedido.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [PlaceOrderUseCase] (éxito vacía el carrito; fallo NO lo vacía).
 * Pista: necesitarás un fake de OrderRepository y FakeCartItemRepository.
 */
class PlaceOrderUseCaseTest {

    lateinit var placeOrderUseCase: PlaceOrderUseCase
    private val orderRepository = FakeOrderRepository()
    private val cartItemRepository = FakeCartItemRepository()
    @Before
    fun setUp() {
        placeOrderUseCase = PlaceOrderUseCase(
            orderRepository = orderRepository,
            cartItemRepository = cartItemRepository,
        )
    }

    @Test
    fun `given successful order when invoke then returns success and clears cart`() = runTest{
        // GIVEN
        val orderConfirmation = orderConfirmation { withOrderId("test-id-1") }
        orderRepository.setOrderConfirmation(orderConfirmation)
        val initialCartItems = listOf(
            cartItem { withQuantity(1) },
            cartItem { withQuantity(2) },
        )
        cartItemRepository.setCartItems(initialCartItems)

        // WHEN
        val result = placeOrderUseCase.invoke()

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == orderConfirmation)
        cartItemRepository.getCartItems().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `given repository throws when invoke then returns failure`() = runTest{
        // GIVEN
        orderRepository.setThrowable(AppError.NetworkError)

        // WHEN
        val result = placeOrderUseCase.invoke()

        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `given repository throws when invoke then does not clear cart`() = runTest{
        // GIVEN
        val orderConfirmation = orderConfirmation { withOrderId("test-id-1") }
        val initialCartItems = listOf(
            cartItem { withQuantity(1) },
            cartItem { withQuantity(2) },
        )
        orderRepository.setOrderConfirmation(orderConfirmation)
        cartItemRepository.setCartItems(initialCartItems)
        orderRepository.setThrowable(AppError.NetworkError)

        // WHEN
        val result = placeOrderUseCase.invoke()

        // THEN
        assertTrue(result.isFailure)
        cartItemRepository.getCartItems().test {
            val items = awaitItem()
            assertTrue(items.isNotEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }
}
