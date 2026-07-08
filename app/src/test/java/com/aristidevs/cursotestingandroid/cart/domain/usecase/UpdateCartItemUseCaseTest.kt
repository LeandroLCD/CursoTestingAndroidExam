package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCartItemUseCaseTest {
    @Test
    fun given_negative_quantity_when_invokes_then_throws_quantity_must_be_positive() =
        runTest {
            // Given
            val fakeProductRepository = FakeProductRepository()
            val fakeCartItemRepository = FakeCartItemRepository()
            val useCase = UpdateCartItemUseCase(fakeCartItemRepository, fakeProductRepository)

            // When
            val exception = runCatching { useCase("id", -1) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun given_zero_quantity_when_invoke_then_removes_items_from_cart() =
        runTest {
            // Given
            val productId = "id1"
            val product =
                product {
                    withId(productId)
                }
            val cartItemProduct =
                cartItem {
                    withProductId(productId)
                    withQuantity(3)
                }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakeCartItemRepository =
                FakeCartItemRepository().apply {
                    setCartItems(listOf(cartItemProduct))
                }
            val useCase = UpdateCartItemUseCase(fakeCartItemRepository, fakeProductRepository)

            // when
            useCase(productId, 0)

            // then
            val items = fakeCartItemRepository.getCartItems().first()
            assertEquals(0, items.size)
        }

    @Test
    fun given_missing_product_when_invoke_then_throws_not_found() =
        runTest {
            // Given
            val fakeProductRepository = FakeProductRepository().apply { setProducts(emptyList()) }
            val fakeCart = FakeCartItemRepository()
            val useCase = UpdateCartItemUseCase(fakeCart, fakeProductRepository)

            // When
            val ex = runCatching { useCase("not", 1) }.exceptionOrNull()

            // Then
            assertTrue(ex is AppError.NotFoundError)
        }

    @Test
    fun given_requested_quantity_greater_than_stock_when_invoke_then_throws_insufficient_stock() =
        runTest {
            // Given
            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                    withStock(3)
                }

            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            val fakeCart =
                FakeCartItemRepository().apply {
                    setCartItems(
                        listOf(
                            cartItem {
                                withProductId(productId)
                                withQuantity(1)
                            },
                        ),
                    )
                }

            val useCase = UpdateCartItemUseCase(fakeCart, fakeProductRepository)

            // When
            val ex = runCatching { useCase(productId, 5) }.exceptionOrNull()

            // Then
            assertTrue(ex is AppError.Validation.InsufficientStock)
        }

    @Test
    fun given_valid_product_and_quantity_when_invoke_then_updates_cart_item() =
        runTest {
            // Given
            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                    withStock(20)
                }

            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            val fakeCart =
                FakeCartItemRepository().apply {
                    setCartItems(
                        listOf(
                            cartItem {
                                withProductId(productId)
                                withQuantity(1)
                            },
                        ),
                    )
                }

            val useCase = UpdateCartItemUseCase(fakeCart, fakeProductRepository)

            // When
            useCase(productId, 5)

            // then
            val items = fakeCart.getCartItems().first()
            assertEquals(1, items.size)
            assertEquals(5, items.first().quantity)
        }
}
