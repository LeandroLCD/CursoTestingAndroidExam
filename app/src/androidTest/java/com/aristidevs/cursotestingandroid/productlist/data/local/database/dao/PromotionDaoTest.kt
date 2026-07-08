package com.aristidevs.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.core.builder.promotionEntity
import com.aristidevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromotionDaoTest {
    private lateinit var database: MiniMarketDatabase

    private lateinit var dao: PromotionDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MiniMarketDatabase::class.java,
                ).build()
        dao = database.promotionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyPromotion_whenGetAllPromotions_thenEmitsEmptyList() =
        runTest {
            val promotions = dao.getAllPromotions().first()
            assertTrue(promotions.isEmpty())
        }

    @Test
    fun givenTwoPromotions_whenInsertAndGetAll_thenEmitsBoth() =
        runTest {
            val promotions =
                listOf(
                    promotionEntity {
                        withId("id1")
                        withPercent(10)
                    },
                    promotionEntity {
                        withId("id2")
                        withPercent(20)
                    },
                )

            dao.insertPromotions(promotions)

            val result = dao.getAllPromotions().first()

            assertEquals(2, result.size)
            assertEquals(10, result.find { it.id == "id1" }?.percent)
            assertEquals(20, result.find { it.id == "id2" }?.percent)
        }

    @Test
    fun givenOldPromotions_whenReplaceAll_thenOnlyNewPromotionsRemain() =
        runTest {
            val promotions =
                listOf(
                    promotionEntity {
                        withId("old-id1")
                        withPercent(10)
                    },
                    promotionEntity {
                        withId("old-id2")
                        withPercent(20)
                    },
                )

            dao.insertPromotions(promotions)

            val newPromotions =
                listOf(
                    promotionEntity {
                        withId("id1")
                        withPercent(10)
                    },
                )

            dao.replaceAll(newPromotions)

            val result =
                dao.getAllPromotions().first()

            assertEquals(1, result.size)
            assertEquals("id1", result.first().id)
        }

    @Test
    fun givenFlowSubscribed_whenInsertAfterSubscribe_thenEmitUpdatedList() =
        runTest {
            dao.getAllPromotions().test {
                val initialValue = awaitItem()
                assertTrue(initialValue.isEmpty())

                dao.insertPromotions(listOf(promotionEntity { withId("1") }))

                val updated = awaitItem()
                assertEquals(1, updated.size)
                assertEquals("1", updated.first().id)
            }
        }
}
