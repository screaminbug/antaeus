package io.pleo.antaeus.core.scheduled

import java.time.DateTimeException
import java.time.YearMonth
import java.util.*
import kotlin.math.min

class MonthlyScheduleProvider : ScheduleProvider {
    override fun getNextDateFromNow(day: Int): Date? {
        return getNextMonthlyScheduledDateForStartDay(day)
    }

    /**
     * Using the current date returns:
     *  - Date with @param startDay of the current month if that day didn't pass
     *  - Date with @param startDay of the next month if that day had already passed
     *  - current Date if @param startDay is the same as the day in the month
     *
     *  @note if @param startDay exceeds number of days in month of the new date it will return
     *  a date with the last day of that month
     *
     *  @param startDay on which day of month to schedule
     */
    private fun getNextMonthlyScheduledDateForStartDay(startDay: Int): Date {
        val now = Calendar.getInstance()
        return getNextMonthlyScheduledDateForStartDay(
            currentYear = now.get(Calendar.YEAR),
            currentMonth = now.get(Calendar.MONTH) + 1,  // Calendar months start from 0
            currentDay = now.get(Calendar.DAY_OF_MONTH),
            startDay = startDay
        )
    }

    /**
     * Takes a date and returns:
     *  - Date with @param startDay of the current month if that day didn't pass
     *  - Date with @param startDay of the next month if dtat day already passed in the month
     *  - the same date if @param startDay is the same as the day in the month
     *
     *  @note if @param startDay exceeds number of days in month of the new date it will return
     *  a date with the last day of that month
     *
     *  @param currentYear current year on which the scheduled date will be calculated
     *  @param currentMonth current month on which the scheduled date will be calculated, 1 is January
     *  @param currentDay current day of month on which the scheduled date will be calculated
     *  @param startDay on which day of month to schedule
     */
    internal fun getNextMonthlyScheduledDateForStartDay(currentYear: Int, currentMonth: Int, currentDay: Int, startDay: Int): Date {

        if (currentMonth > 12 || currentMonth < 1) { throw DateTimeException("Month cannot be greater than 12 and less than 1. Was $currentMonth") }

        val daysInThisMonth = getDaysInMonth(currentYear, currentMonth)
        if (currentDay > daysInThisMonth) {
            throw DateTimeException(
                "Current day $currentDay for month $currentMonth of year $currentYear " +
                        "cannot be greater than $daysInThisMonth.")
        }

        val daysInNextMonth = getDaysInNextMonth(currentYear, currentMonth)

        val startDayThisMonth = min(startDay, daysInThisMonth)
        val startDayNextMonth = min(startDay, daysInNextMonth)


        val fromWhen = Calendar.getInstance()
        val currentCalendarMonth = currentMonth - 1
        fromWhen.set(currentYear, currentCalendarMonth, currentDay)

        // if we haven't reached start day this month, set start date to
        // startDay but capped at maximum number of days of this month
        // for example if the date is 3rd of February, and startDay is 31
        // we will start on 28th or 29th of February, depending if it's a leap year or not
        if (currentDay < startDayThisMonth) {
            fromWhen.set(Calendar.DAY_OF_MONTH, startDayThisMonth)

            // if we already passed startDay in current month, we'll add one month
            // but with startDay that is capped to the maximum number of days of the next month
            // to not overflow to the next month
        } else if (currentDay > startDayThisMonth) {
            if (currentDay > startDayNextMonth) { fromWhen.set(Calendar.DAY_OF_MONTH, startDayNextMonth) }
            fromWhen.add(Calendar.MONTH, 1)
        }

        // otherwise supplied date is the date we start

        return fromWhen.time
    }

    private fun getDaysInNextMonth(year: Int, month: Int): Int {
        val actualYear = if (month + 1 <= 12) year else year + 1
        val actualMonth = 1 + month % 12
        return getDaysInMonth(actualYear, actualMonth)
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val yearMonth = YearMonth.of(year, month)
        return yearMonth.lengthOfMonth()
    }
}