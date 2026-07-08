package com.aristidevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.JsonUtils.readJson
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PromotionRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Before
    fun setUp() {
        hilt.inject()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenActivePromotionsJson_whenRefreshIsCalled_thenFlowEmitsActivePromotions() =
        runTest {
            val json = readJson("promotions_percent.json")
            mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))

            promotionRepository.refreshPromotions()

            val promotions = promotionRepository.getActivePromotions().first()

            assertTrue(promotions.isNotEmpty())
        }

    @Test
    fun givenEmptyPromotionsJson_whenRefreshIsCalled_thenListIsEmpty() =
        runTest {
            mockWebServer.server.enqueue(
                MockResponse().setBody("""{"promotions":[]}""").setResponseCode(200),
            )

            promotionRepository.refreshPromotions()

            val promotions = promotionRepository.getActivePromotions().first()

            assertTrue(promotions.isEmpty())
        }

    @Test
    fun givenBuyXPayYJson_whenRefreshIsCalled_thenDomainMapsQuantitiesCorrectly() =
        runTest {
            val json = readJson("promotions_buy_x_pay_y.json")
            mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))

            promotionRepository.refreshPromotions()
            val promotion = promotionRepository.getActivePromotions().first().find { it.id == "bxpy" }

            assertNotNull(promotion)
            assertEquals(2.0, promotion?.value)
            assertEquals(3, promotion?.buyQuantity)
        }

    @Test(expected = Exception::class)
    fun givenServerReturns500_whenRefreshIsCalled_thenItThrowsException() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setResponseCode(500))
            promotionRepository.refreshPromotions()
        }

    @Test
    fun givenPromotionEndpoint_whenRefreshIsCalled_thenRequestIsGetToCorrectPath() =
        runTest {
            val json = readJson("promotions_buy_x_pay_y.json")
            mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))
            promotionRepository.refreshPromotions()

            val request = mockWebServer.server.takeRequest()
            assertEquals("GET", request.method)
            assertTrue(request.path?.contains("data/promotions.json") == true)
        }
}
