package com.aristidevs.cursotestingandroid.cart.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.builder.cartItemEntity
import com.aristidevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartItemDaoTest {
    private lateinit var database: MiniMarketDatabase

    private lateinit var dao: CartItemDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MiniMarketDatabase::class.java,
                ).build()
        dao = database.cartItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyCart_whenGetAllCarItems_thenEmitsEmptyList() =
        runTest {
            val item = dao.getAllCartItems().first()

            assertTrue(item.isEmpty())
        }

    @Test
    fun givenEmptyCart_whenInsertItem_thenItemIsPersisted() =
        runTest {
            val id = "id"
            val quantity = 3
            val item =
                cartItemEntity {
                    withProductId(id)
                    withQuantity(quantity)
                }

            dao.insertCartItem(item)

            val result = dao.getAllCartItems().first()

            assertEquals(1, result.size)
            assertEquals(quantity, result.first().quantity)
            assertEquals(id, result.first().productId)
        }

    @Test
    fun givenInsertedItem_whenGetItemById_thenReturnsCorrectItem() =
        runTest {
            dao.insertCartItem(
                cartItemEntity {
                    withProductId("id")
                    withQuantity(3)
                },
            )

            val result = dao.getCartItemById("id")

            assertEquals("id", result?.productId)
            assertEquals(3, result?.quantity)
        }

    @Test
    fun givenEmptyCart_whenGetItemById_thenReturnsNull() =
        runTest {
            val result = dao.getCartItemById("id")

            assertNull(result)
        }

    @Test
    fun givenExistingItem_whenUpdateItemQuantity_thenQuantityIsUpdated() =
        runTest {
            val id = "id"
            dao.insertCartItem(
                cartItemEntity {
                    withProductId(id)
                    withQuantity(1)
                },
            )
            dao.updateCartItem(
                cartItemEntity {
                    withProductId(id)
                    withQuantity(67)
                },
            )

            val result = dao.getCartItemById(id)

            assertEquals(67, result?.quantity)
        }

    @Test
    fun givenItemCart_whenDeleteItem_thenCartBecomesEmpty() =
        runTest {
            val p =
                cartItemEntity {
                    withProductId("id")
                    withQuantity(1)
                }
            dao.insertCartItem(p)

            dao.deleteCartItem(p)

            val result = dao.getAllCartItems().first()
            assertTrue(result.isEmpty())
        }

    @Test
    fun givenMultipleItems_whenClearCart_thenAllItemsAreRemoved() =
        runTest {
            val p1 =
                cartItemEntity {
                    withProductId("id1")
                    withQuantity(1)
                }
            val p2 =
                cartItemEntity {
                    withProductId("id2")
                    withQuantity(1)
                }
            val p3 =
                cartItemEntity {
                    withProductId("id3")
                    withQuantity(1)
                }

            dao.insertCartItem(p1)
            dao.insertCartItem(p2)
            dao.insertCartItem(p3)

            dao.clearCart()

            val result = dao.getAllCartItems().first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun givenExistingItemId_whenInsertDuplicateId_thenItemIsReplaced() =
        runTest {
            val p1 =
                cartItemEntity {
                    withProductId("id1")
                    withQuantity(1)
                }
            val p2 =
                cartItemEntity {
                    withProductId("id1")
                    withQuantity(27)
                }

            dao.insertCartItem(p1)
            dao.insertCartItem(p2)

            val result = dao.getAllCartItems().first()

            assertEquals(27, result.first().quantity)
        }
}
