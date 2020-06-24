package formsmanager.businesscalendar

import java.time.*
import java.util.function.Predicate

object BusinessCalendarUtils {

    fun betweenHours(start: LocalTime, stop: LocalTime): Predicate<ZonedDateTime> {
        return Predicate {
            val localTime = it.toLocalTime()
            (localTime == start || localTime == stop) || (localTime.isAfter(start) && localTime.isBefore(stop))
        }
    }

    fun betweenHours(start: String, stop: String): Predicate<ZonedDateTime> {
        return betweenHours(LocalTime.parse(start), LocalTime.parse(stop))
    }

    fun specificDate(vararg dates: LocalDate): Predicate<ZonedDateTime> {
        return Predicate { submission -> dates.any { LocalDate.from(submission) == it } }
    }

    fun date(vararg dates: MonthDay): Predicate<ZonedDateTime> {
        return Predicate { submission -> dates.any { MonthDay.from(submission) == it } }
    }

    fun date(vararg dates: String): Predicate<ZonedDateTime> {
        val monthDays = dates.map { MonthDay.parse("--$it") }
        return date(*monthDays.toTypedArray())
    }

    fun dayOfWeek(vararg dayOfWeek: DayOfWeek): Predicate<ZonedDateTime> {
        return Predicate { submission -> dayOfWeek.any { DayOfWeek.from(submission) == it } }
    }

    fun month(vararg dates: Month): Predicate<ZonedDateTime> {
        return Predicate { submission -> dates.any { Month.from(submission) == it } }
    }

    fun weekend(weekend: Array<DayOfWeek> = arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)): Predicate<ZonedDateTime> {
        return Predicate { it.dayOfWeek in weekend }
    }

    fun weekday(weekday: Array<DayOfWeek> = arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)): Predicate<ZonedDateTime> {
        return Predicate { it.dayOfWeek in weekday }
    }

    fun everyday(): Predicate<ZonedDateTime> {
        return Predicate { true }
    }

}