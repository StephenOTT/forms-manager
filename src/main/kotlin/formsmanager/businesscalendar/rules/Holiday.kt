package formsmanager.businesscalendar.rules

import formsmanager.businesscalendar.BusinessCalendarRule
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

data class Holiday(
        val holidayStrings: List<String>,
        val incrementer: (now: ZonedDateTime) -> Duration = { Duration.ofDays(1) }
) : BusinessCalendarRule {

    val holidayDates: List<LocalDate> = holidayStrings.map { LocalDate.parse(it) }

    override fun incrementer(date: ZonedDateTime): Duration {
        return incrementer.invoke(date)
    }

    override fun test(t: ZonedDateTime): Boolean {
        return t.toLocalDate() !in holidayDates
    }
}