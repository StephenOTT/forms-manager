package formsmanager.camunda.engine.businesscalendar.calendars

import formsmanager.camunda.engine.businesscalendar.CustomBusinessCalendar
import formsmanager.camunda.engine.businesscalendar.CustomBusinessCalendar.Companion.betweenHours
import formsmanager.camunda.engine.businesscalendar.CustomBusinessCalendar.Companion.isDate
import formsmanager.camunda.engine.businesscalendar.CustomBusinessCalendar.Companion.isWeekday
import java.time.Instant
import java.time.ZonedDateTime
import java.util.function.Predicate
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("StdWorkWeek")
class StdWorkWeekCalendar : CustomBusinessCalendar {
    override val name: String = "StdWorkWeek"

    override val rules: List<Predicate<ZonedDateTime>> = listOf(
            isWeekday()
                    .and(betweenHours("09:00", "17:00"))
                    .and(betweenHours("12:00", "12:59").negate())
                    .and(isDate("06-01").negate())
    )
    override val incrementer: ((date: Instant) -> Instant)? = null
}