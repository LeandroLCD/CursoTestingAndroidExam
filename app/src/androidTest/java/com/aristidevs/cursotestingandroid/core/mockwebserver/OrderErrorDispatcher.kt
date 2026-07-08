package com.aristidevs.cursotestingandroid.core.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class OrderErrorDispatcher(
    private val httpCode: Int = 500,
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse =
        MockResponse()
            .setResponseCode(httpCode)
}
