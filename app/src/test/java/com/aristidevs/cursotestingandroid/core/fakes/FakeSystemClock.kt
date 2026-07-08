package com.aristidevs.cursotestingandroid.core.fakes

import com.aristidevs.cursotestingandroid.core.domain.util.Clock
import java.time.Instant

class FakeSystemClock : Clock {
    private var currentTime: Instant = Instant.now()

    fun setTime(time: Instant) {
        currentTime = time
    }

    fun advanceTime(seconds: Long) {
        currentTime = currentTime.plusSeconds(seconds)
    }

    override fun now(): Instant = currentTime
}
