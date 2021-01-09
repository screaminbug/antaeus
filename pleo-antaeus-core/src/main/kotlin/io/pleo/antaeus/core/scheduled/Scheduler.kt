package io.pleo.antaeus.core.scheduled

import java.util.*

interface Scheduler {
    fun schedule(startDay: Int)
}