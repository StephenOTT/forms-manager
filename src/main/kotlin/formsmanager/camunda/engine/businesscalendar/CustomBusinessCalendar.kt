package formsmanager.camunda.engine.businesscalendar

import java.time.*
import java.util.function.Predicate

/**
 * Interface for creating Custom Business Calendars.
 * These calendars are business level implementations of a calendar rules.
 * Not to be confused with Cycle, DueDate, and Duration "Business Calendars" which are the Timer configuration calendars.
 */
interface CustomBusinessCalendar {

    /**
     * Name of the business calendar.  Used in the BPMN configuration: `(MyCalendar)PT5M`
     */
    val name: String

    /**
     * List of Predicates that are evaluated against the possible due date for the timer
     */
    val rules: List<Predicate<ZonedDateTime>>

    /**
     * Optional incrementer used for DueDate and Duration Timer configurations.
     * The incrementer is logic to increment the next possible due date.
     * This logic is needed for DueDate and Duration because there is no built in logic of those types to
     * explain how to try the next possible date.
     *
     */
    val incrementer: ((date: Instant) -> Instant)?

    /**
     * Evaluate date against a predicate.
     * Use Predicate chains such as `.and(...)` to combine multiple predicates
     */
    fun evaluate(date: ZonedDateTime): Boolean {
        return rules.all {
            it.test(date)
        }
    }

    companion object {

        val customCalendarRegex = Regex("(^\\([a-zA-Z0-9_-]*\\))(\\V*)")

        /**
         * @param calendarName the name of the calendar.  This is the value inside of the round brackets.
         * @param duedateDescription the due date description such as: `PT5M`.
         */
        data class CustomCalendarDescription(
                val calendarName: String,
                val duedateDescription: String
        )

        /**
         * @return CustomCalendarDescription or null if there is no custom calendar
         * @exception IllegalArgumentException if calendar was detected but unable to parse
         */
        fun getCustomCalendar(duedateDescription: String): CustomCalendarDescription? {
            //@TODO refactor the startsWith parsing
            return if (duedateDescription.startsWith("(")) {
                val result = customCalendarRegex.find(duedateDescription)
                return if (result != null) {
                    val calendarName = result.groupValues[1].removePrefix("(").removeSuffix(")")
                    val dueDateText = result.groupValues[2]
                    CustomCalendarDescription(calendarName, dueDateText)
                } else {
                    throw IllegalArgumentException("Unable to parse custom calendar name")
                }
            } else {
                null
            }
        }

        fun isDate(vararg dates: MonthDay): Predicate<ZonedDateTime> {
            return Predicate { submission -> dates.all { MonthDay.from(submission) == it } }
        }

        fun isDate(vararg dates: String): Predicate<ZonedDateTime> {
            val monthDays = dates.map { MonthDay.parse("--$it") }
            return isDate(*monthDays.toTypedArray())
        }

        fun isSpecificDate(vararg dates: LocalDate): Predicate<ZonedDateTime> {
            return Predicate { submission -> dates.all { LocalDate.from(submission) == it } }
        }

        fun isSpecificDate(vararg dates: String): Predicate<ZonedDateTime> {
            val localDates = dates.map { LocalDate.parse(it) }
            return isSpecificDate(*localDates.toTypedArray())
        }

        fun betweenHours(start: LocalTime, stop: LocalTime): Predicate<ZonedDateTime> {
            return Predicate {
                val localTime = it.toLocalTime()
                (localTime == start || localTime == stop) || (localTime.isAfter(start) && localTime.isBefore(stop))
            }
        }

        fun betweenHours(start: String, stop: String): Predicate<ZonedDateTime> {
            return betweenHours(LocalTime.parse(start), LocalTime.parse(stop))
        }

        fun isDayOfWeek(vararg dayOfWeek: DayOfWeek): Predicate<ZonedDateTime> {
            return Predicate { submission -> dayOfWeek.all { DayOfWeek.from(submission) == it } }
        }

        fun isDayOfWeek(vararg dayOfWeek: String): Predicate<ZonedDateTime> {
            val months = dayOfWeek.map { DayOfWeek.valueOf(it.toUpperCase()) }
            return isDayOfWeek(*months.toTypedArray())
        }

        fun isMonth(vararg dates: Month): Predicate<ZonedDateTime> {

            return Predicate { submission -> dates.all { Month.from(submission) == it } }
        }

        fun isMonth(vararg dates: String): Predicate<ZonedDateTime> {
            val months = dates.map { Month.valueOf(it.toUpperCase()) }
            return isMonth(*months.toTypedArray())
        }

        fun isWeekend(weekend: Array<DayOfWeek> = arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)): Predicate<ZonedDateTime> {
            return Predicate { it.dayOfWeek in weekend }
        }

        fun isWeekday(weekday: Array<DayOfWeek> = arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)): Predicate<ZonedDateTime> {
            return Predicate { it.dayOfWeek in weekday }
        }
    }
}