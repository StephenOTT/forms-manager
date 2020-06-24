package formsmanager.businesscalendar.rules

import formsmanager.businesscalendar.BusinessCalendarRule
import java.time.Duration
import java.time.ZonedDateTime
import java.util.function.Predicate

data class IfIs(
        val rule: Predicate<ZonedDateTime>,
        val subRule: Predicate<ZonedDateTime>,
        val incrementer: (date: ZonedDateTime) -> Duration = { Duration.ofHours(1) }
) : BusinessCalendarRule {
    override fun incrementer(date: ZonedDateTime): Duration {
        return incrementer.invoke(date)
    }

    override fun test(t: ZonedDateTime): Boolean {
        return if (rule.test(t)) {
            subRule.test(t)
        } else {
            true
        }
    }
}