package formsmanager.businesscalendar

import java.time.Duration
import java.time.ZonedDateTime
import java.util.function.Predicate

interface BusinessCalendarRule : Predicate<ZonedDateTime> {
    fun incrementer(date: ZonedDateTime): Duration
}