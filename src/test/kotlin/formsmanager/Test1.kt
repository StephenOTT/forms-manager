package formsmanager

import formsmanager.businesscalendar.BusinessCalendarUtils.betweenHours
import formsmanager.businesscalendar.BusinessCalendarUtils.date
import formsmanager.businesscalendar.BusinessCalendarUtils.weekday
import formsmanager.businesscalendar.BusinessCalendarUtils.weekend
import formsmanager.businesscalendar.StdBusinessCalendar
import formsmanager.businesscalendar.rules.Holiday
import formsmanager.businesscalendar.rules.IfIs
import formsmanager.businesscalendar.rules.Is
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime


class HelloControllerTest {

    @Test
    fun testGreetingService() {
        val bc = StdBusinessCalendar(
                listOf(
                        Holiday(listOf("2020-06-24", "2020-06-25", "2020-06-26")), // Cannot be any of these dates
                        Is(weekday()), // Must be a weekday (no weekends)
                        IfIs(date("06-29"), betweenHours("10:00", "12:30")) {
                            if (it.toLocalTime().isBefore(LocalTime.of(12, 30))) {
                                Duration.ofMinutes(1)
                            } else {
                                Duration.ofDays(1)
                            }
                        }, // Partial Holiday
                        Holiday(listOf("2020-06-23")) // Today is a holiday so its a extra day
                )
        )

        val now = ZonedDateTime.of(2020, 6, 23, 9, 0, 0, 0, ZoneId.systemDefault())

        val result = bc.nextAvailableDate(now).get()
        assertEquals(result, ZonedDateTime.of(2020, 6, 29, 10, 0, 0, 0, ZoneId.systemDefault()))
        println(result)

        val incrementResult = bc.nextAvailableDateDayIncrement(now, 2).get()
        assertEquals(incrementResult, ZonedDateTime.of(2020, 7, 1, 10, 0, 0, 0, ZoneId.systemDefault()))

        println(incrementResult)

        val bc2 = StdBusinessCalendar(
                listOf(
                        Holiday(listOf("2020-06-24", "2020-06-25", "2020-06-26")), // Cannot be any of these dates
                        Is(weekday()), // Must be a weekday (no weekends)
                        IfIs(date("06-29"), betweenHours("10:00", "12:30")) {
                            if (it.toLocalTime().isBefore(LocalTime.of(12, 30))) {
                                Duration.ofMinutes(1)
                            } else {
                                Duration.ofDays(1)
                            }
                        } // Partial Holiday
//                        Holiday(listOf("2020-06-23")) // Today is a holiday so its a extra day
                )
        )

        val incrementResult2 = bc2.nextAvailableDateDayIncrement(now, 2, true).get()
        println(incrementResult2)
        assertEquals(incrementResult, ZonedDateTime.of(2020, 7, 1, 10, 0, 0, 0, ZoneId.systemDefault()))

        val incrementResult3 = bc2.nextAvailableDateDayIncrement(now, 2, false).get()
        println(incrementResult2)
        assertEquals(incrementResult, ZonedDateTime.of(2020, 6, 30, 10, 0, 0, 0, ZoneId.systemDefault()))
    }

    @Test
    fun TenBusinessDays() {
        //@TODO add Holiday Object that can read from tzdata localized holidays.
        //@TODO add example of a calculation of "Not the Second Monday of the Month in N Month"
        // @TODO add a hours incrementer for actions like "within 48 hours"
        val bc = StdBusinessCalendar(
                listOf(
                        Holiday(listOf("2020-06-24", "2020-06-25", "2020-06-26")), // Cannot be any of these dates
                        Is(weekday()), // Must be a weekday (no weekends)
                        Is(weekend().negate()), // example of saying "not a weekend"
                        IfIs(weekday(), betweenHours("09:00", "17:30")) {
                            // Standard workweek:
                            if (it.toLocalTime().isBefore(LocalTime.of(17, 30))) {
                                Duration.ofMinutes(1)
                            } else {
                                Duration.ofDays(1)
                            }
                        },
                        IfIs(date("06-29"), betweenHours("10:00", "12:30")) {
                            // modification to a standard work week with a partial day
                            if (it.toLocalTime().isBefore(LocalTime.of(12, 30))) {
                                // If it's before 12:30 then increment by 1min
                                // Of course you could add more complex logic to detect current time and jump to best starting point.
                                Duration.ofMinutes(1)
                            } else {
                                // If it is already past 12:30 then go to next day
                                Duration.ofDays(1)
                            }
                        },
                        Holiday(listOf("2020-06-23"))
                )
        )

        val now = ZonedDateTime.of(2020, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault())

        val result = bc.nextAvailableDateDayIncrement(now, 10, true).get() // Start Next day
        assertEquals(result, ZonedDateTime.of(2020, 6, 15, 9, 0, 0, 0, ZoneId.systemDefault()))

        val result2 = bc.nextAvailableDateDayIncrement(now, 10, false).get() // Count starts on same day
        assertEquals(result2, ZonedDateTime.of(2020, 6, 12, 9, 0, 0, 0, ZoneId.systemDefault()))

        val now3 = ZonedDateTime.of(2020, 6, 29, 6, 0, 0, 0, ZoneId.systemDefault())
        val result3 = bc.nextAvailableDate(now3).get() // Count starts on same day
        assertEquals(result3, ZonedDateTime.of(2020, 6, 29, 10, 0, 0, 0, ZoneId.systemDefault()))
    }
}