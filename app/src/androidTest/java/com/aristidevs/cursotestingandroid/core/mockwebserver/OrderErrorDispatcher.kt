package com.aristidevs.cursotestingandroid.core.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class OrderErrorDispatcher(private val httpCode: Int = 500) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse =
        when {
            request.path?.contains("order_confirmation.json") == true ->
                MockResponse()
                    .setResponseCode(httpCode)
            else -> MockResponse().setResponseCode(404)
        }
}
