package com.aristidevs.cursotestingandroid.core.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MiniMarketApiDispatcher(
    private val productJson: String,
    private val promoJson: String = """{"promotions":[]}""",
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
            else -> MockResponse().setResponseCode(404)
        }
}
