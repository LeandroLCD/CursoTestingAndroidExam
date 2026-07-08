package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddToCartUseCaseTest {
    @Test
    fun zero_quantity_throws_QuantityMustBePositive() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartItemRepository()
            val fakeProductRepository = FakeProductRepository()
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)

            // When
            val exception = runCatching { useCase("id", 0) }.exceptionOrNull()

            // then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun negative_quantity_throws_QuantityMustBePositive() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartItemRepository()
            val fakeProductRepository = FakeProductRepository()
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)

            // When
            val exception = runCatching { useCase("id", -2) }.exceptionOrNull()

            // then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun non_existing_product_throws_NotFountError() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartItemRepository()
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(emptyList())
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)

            // When
            val exception = runCatching { useCase("id", 1) }.exceptionOrNull()

            // then
            assertTrue(exception is AppError.NotFoundError)
        }

    @Test
    fun insufficient_stock_throws_InsufficientStock() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(2)
                }
            val fakeCartRepository = FakeCartItemRepository()
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)

            // when
            val exception =
                runCatching {
                    useCase(productId, 5)
                }.exceptionOrNull()

            assertTrue(exception is AppError.Validation.InsufficientStock)
            assertEquals(2, (exception as AppError.Validation.InsufficientStock).available)
        }

    @Test
    fun successful_case_adds_item_ti_cart() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            val fakeCartRepository = FakeCartItemRepository()
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)

            // when
            useCase(productId, 3)

            // then
            val items = fakeCartRepository.getCartItems().first()
            assertEquals(productId, items.first().productId)
            assertEquals(1, items.size)
            assertEquals(3, items.first().quantity)
        }

    @Test
    fun default_quantity_adds_one_item() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            val fakeCartRepository = FakeCartItemRepository()
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)

            // when
            useCase(productId)

            // then
            val items = fakeCartRepository.getCartItems().first()
            assertEquals(1, items.size)
            assertEquals(1, items.first().quantity)
        }

    @Test
    fun zero_quantity_does_not_call_any_repository() =
        runTest {
            // Given
            val productRepository = mockk<ProductRepository>()
            val cartRepository = mockk<CartItemRepository>()
            val useCase = AddToCartUseCase(cartRepository, productRepository)

            // When
            runCatching { useCase("id", 0) }.exceptionOrNull()

            // then
            coVerify(exactly = 0) { productRepository.getProductById(any()) }
            coVerify(exactly = 0) { cartRepository.getCartItemById(any()) }
            coVerify(exactly = 0) { cartRepository.addToCart(any(), any()) }
        }

    @Test
    fun valid_product_calls_addToCart_with_expect_values() =
        runTest {
            // Given
            val productRepository = mockk<ProductRepository>()
            val cartRepository = mockk<CartItemRepository>()

            val productId = "custom-id"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }

            coEvery { productRepository.getProductById(productId) } returns flowOf(product)
            coEvery { cartRepository.getCartItemById(productId) } returns null
            coEvery { cartRepository.addToCart(productId, 3) } just Runs

            val useCase = AddToCartUseCase(cartRepository, productRepository)

            // when
            useCase(productId, 3)

            // then
            coVerify(exactly = 1) { productRepository.getProductById(productId) }
            coVerify(exactly = 1) { cartRepository.getCartItemById(productId) }
            coVerify(exactly = 1) { cartRepository.addToCart(productId, 3) }
        }
}
