package com.aristidevs.cursotestingandroid.core.data.util

import com.aristidevs.cursotestingandroid.core.domain.util.Clock
import java.time.Instant
import javax.inject.Inject

class SystemClock
    @Inject
    constructor() : Clock {
        override fun now(): Instant = Instant.now()
    }
