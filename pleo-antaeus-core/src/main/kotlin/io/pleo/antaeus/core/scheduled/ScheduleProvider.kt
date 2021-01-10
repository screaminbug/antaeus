package io.pleo.antaeus.core.scheduled

import io.pleo.antaeus.core.util.getNextMonthlyScheduledDateForStartDay
import java.util.*

class ScheduleProvider {
    fun getForNow(day: Int): Date? {
        return getNextMonthlyScheduledDateForStartDay(day)
    }

    fun getForAny(scheduleDay: Int, currentYear: Int, currentMonth: Int, currentDay: Int): Date {
        return getNextMonthlyScheduledDateForStartDay(currentYear, currentMonth, currentDay, scheduleDay)
    }
}