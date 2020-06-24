package formsmanager.businesscalendar.rules

import formsmanager.businesscalendar.BusinessCalendarRule
import java.time.Duration
import java.time.ZonedDateTime
import java.util.function.Predicate

data class Is(
        val rule: Predicate<ZonedDateTime>,
        val incrementer: (now: ZonedDateTime) -> Duration = { Duration.ofDays(1) }
) : BusinessCalendarRule {
    override fun incrementer(date: ZonedDateTime): Duration {
        return incrementer.invoke(date)
    }

    override fun test(t: ZonedDateTime): Boolean {
        return rule.test(t)
    }
}