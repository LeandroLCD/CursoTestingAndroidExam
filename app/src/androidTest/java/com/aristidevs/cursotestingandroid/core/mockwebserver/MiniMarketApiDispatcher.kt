package com.aristidevs.cursotestingandroid.core.mockwebserver

import com.aristidevs.cursotestingandroid.core.utils.asAsset
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MiniMarketApiDispatcher(
    private val productJson: String,
    private val promoJson: String = """{"promotions":[]}""",
    private val orderConfirmationJson: String = "order_confirmation.json".asAsset(),
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse =
        when {
            request.path?.contains("promotions.json") == true ->
                MockResponse()
                    .setBody(promoJson)
                    .setResponseCode(200)
            request.path?.contains("products.json") == true ->
                MockResponse()
                    .setBody(productJson)
                    .setResponseCode(200)
            request.path?.contains("order_confirmation.json") == true -> {
                MockResponse()
                    .setBody(orderConfirmationJson)
                    .setResponseCode(200)
            }
            else -> MockResponse().setResponseCode(404)
        }
}
