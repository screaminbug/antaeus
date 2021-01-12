package io.pleo.antaeus.core.scheduled

import java.util.*

interface ScheduleProvider {
    fun getNextDateFromNow(day: Int): Date?
}
