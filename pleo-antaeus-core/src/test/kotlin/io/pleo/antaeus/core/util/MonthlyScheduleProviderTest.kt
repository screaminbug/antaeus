package io.pleo.antaeus.core.util

import io.pleo.antaeus.core.scheduled.MonthlyScheduleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.DateTimeException
import java.time.YearMonth

internal class MonthlyScheduleProviderTest {
    @Test
    fun `given all dates up do 28th in month should return next correct scheduled date`() {
        (2000..2100).forEach {
            year -> (1..12).forEach {
                month -> (1..getMaxDaysInMonth(year, month)).forEach {
                    day -> (1..28).forEach {
                        expectedDay -> assertCorrectDateSet(year, month, day, expectedDay)
                    }
                }
            }
        }
    }

    @Test
    fun `given leap year and current date 31st january and start day 30th, should return 29th of february`() {
        assertCorrectDateSet(
            currentYear = 2000,
            currentMonth = 1,
            currentDay = 31,
            startDay = 30,

            expectedYear = 2000,
            expectedMonth = 2,
            expectedDay = 29
        )
    }

    @Test
    fun `given non leap year and current date 31st january and start day 30th, should return 28th of february`() {
        assertCorrectDateSet(
            currentYear = 2001,
            currentMonth = 1,
            currentDay = 31,
            startDay = 30,

            expectedYear = 2001,
            expectedMonth = 2,
            expectedDay = 28
        )
    }

    @Test
    fun `given leap year and current date 28th of february and start date 31th, should return 29th of february`() {
        assertCorrectDateSet(
            currentYear = 2000,
            currentMonth = 2,
            currentDay = 28,
            startDay = 31,

            expectedYear = 2000,
            expectedMonth = 2,
            expectedDay = 29
        )
    }

    @Test
    fun `given non leap year and current date 28th of february and start date 31th, should return 28th of february`() {
        assertCorrectDateSet(
            currentYear = 2001,
            currentMonth = 2,
            currentDay = 28,
            startDay = 31,

            expectedYear = 2001,
            expectedMonth = 2,
            expectedDay = 28
        )
    }

    @Test
    fun `given current date 31th of december and start date 30th, should return 30th of january next year`() {
        assertCorrectDateSet(
            currentYear = 2001,
            currentMonth = 12,
            currentDay = 31,
            startDay = 30,

            expectedYear = 2002,
            expectedMonth = 1,
            expectedDay = 30
        )
    }

    @Test
    fun `given invalid month 0, should throw DateTimeException`() {
        assertThrows(DateTimeException::class.java, {
            assertCorrectDateSet(
                currentYear = 2001,
                currentMonth = 0,
                currentDay = 31,
                startDay = 30,

                expectedYear = 2002,
                expectedMonth = 1,
                expectedDay = 30
            )
        }, "for invalid month 0 should throw")
    }

    @Test
    fun `given invalid month 13, should throw DateTimeException`() {
        assertThrows(DateTimeException::class.java, {
            assertCorrectDateSet(
                currentYear = 2001,
                currentMonth = 13,
                currentDay = 31,
                startDay = 30,

                expectedYear = 2002,
                expectedMonth = 1,
                expectedDay = 30
            )
        }, "for invalid month 13 should throw")
    }

    @Test
    fun `given invalid date in month, should throw DateTimeException`() {
        assertThrows(DateTimeException::class.java, {
            assertCorrectDateSet(
                currentYear = 2001,
                currentMonth = 2,
                currentDay = 29,
                startDay = 30,

                expectedYear = 2001,
                expectedMonth = 2,
                expectedDay = 29
            )
        }, "for invalid date 29 in month february should throw")
    }



    private fun assertCorrectDateSet(year: Int, month: Int, day: Int, startDay: Int) {
        val calendarYear = year % 2000 + 100 // works only for year 2000 and later, but it's ok because we won't be scheduling in the past
        val expectedCalendarYear = if (startDay < day && month + 1 > 12) calendarYear + 1 else calendarYear
        val calendarMonth = month - 1
        val expectedCalendarMonth = if (startDay < day) (calendarMonth + 1) % 12 else calendarMonth % 12

        val calculated = MonthlyScheduleProvider().getNextMonthlyScheduledDateForStartDay(year, month, day, startDay)
        assertEquals(expectedCalendarYear , calculated.year, "Year: $year, Month: $month, Day: $day, Expected day: $startDay -- Invalid YEAR!")
        assertEquals(expectedCalendarMonth, calculated.month, "Year: $year, Month: $month, Day: $day, Expected day: $startDay -- Invalid MONTH!")
        assertEquals(startDay, calculated.date, "Year: $year, Month: $month, Day: $day, Expected day: $startDay -- Invalid DAY!")
    }

    private fun assertCorrectDateSet(
        currentYear: Int,
        currentMonth: Int,
        currentDay: Int,
        startDay: Int,
        expectedYear: Int,
        expectedMonth: Int,
        expectedDay: Int
    ) {
        val expectedCalendarYear = expectedYear % 2000 + 100
        val expectedCalendarMonth = expectedMonth - 1

        val calculated = MonthlyScheduleProvider().getNextMonthlyScheduledDateForStartDay(currentYear, currentMonth, currentDay, startDay)

        assertEquals(expectedCalendarYear , calculated.year, "Year: $currentYear, Month: $currentMonth, Day: $currentDay, Expected day: $expectedDay -- Invalid YEAR!")
        assertEquals(expectedCalendarMonth, calculated.month, "Year: $currentYear, Month: $currentMonth, Day: $currentDay, Expected day: $expectedDay -- Invalid MONTH!")
        assertEquals(expectedDay, calculated.date, "Year: $currentYear, Month: $currentMonth, Day: $currentDay, Expected day: $expectedDay -- Invalid DAY!")


    }

    private fun getMaxDaysInMonth(year: Int, month: Int): Int {
        val yearMonth = YearMonth.of(year, month)
        return yearMonth.lengthOfMonth()
    }

}