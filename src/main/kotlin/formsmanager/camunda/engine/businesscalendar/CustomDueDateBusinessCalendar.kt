package formsmanager.camunda.engine.businesscalendar

import org.camunda.bpm.engine.impl.ProcessEngineLogger
import org.camunda.bpm.engine.impl.calendar.DueDateBusinessCalendar
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Singleton

/**
 * Extending the DueDateBusinessCalendar to provide custom business calendar injection logic
 */
@Singleton
class CustomDueDateBusinessCalendar(
        private val calendars: List<CustomBusinessCalendar>
): BusinessCalendar, DueDateBusinessCalendar() {

    override val calendarName: String = NAME

    private val LOG = ProcessEngineLogger.UTIL_LOGGER

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    override fun resolveDuedate(duedate: String, startDate: Date?): Date {
        val customCalendarDescription: CustomBusinessCalendar.Companion.CustomCalendarDescription? = CustomBusinessCalendar.getCustomCalendar(duedate)
        return if (customCalendarDescription != null){
            val customCalendar = calendars.single { it.name == customCalendarDescription.calendarName }

            val initial: Date = super.resolveDuedate(customCalendarDescription.duedateDescription, startDate)

            val customCalendarEvalResult: Boolean = customCalendar.evaluate(ZonedDateTime.ofInstant(initial.toInstant(), ZoneId.systemDefault()))

            return if (customCalendarEvalResult) {
                println("accepted Eval:" + initial)
                initial
            } else {
                val incrementer = customCalendar.incrementer
                if (incrementer != null){
                    val incrementedInstant = incrementer.invoke(
                            initial.toInstant()).atZone(ZoneId.systemDefault()
                    )
                    val output = formatter.format(incrementedInstant)
                    println("try again Eval with increment:" + output)
                    resolveDuedate("(${customCalendarDescription.calendarName})$output", startDate)
                } else {
                    throw LOG.exceptionWhileResolvingDuedate(duedate, IllegalArgumentException("Duedate is invalid for provided business calendar."))
                }
            }

        } else {
            super.resolveDuedate(duedate, startDate)
        }
    }
}