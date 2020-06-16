package formsmanager.camunda.engine.businesscalendar

import org.camunda.bpm.engine.impl.ProcessEngineLogger
import org.camunda.bpm.engine.impl.calendar.DurationBusinessCalendar
import org.camunda.bpm.engine.impl.calendar.DurationHelper
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton


/**
 * Extending the DurationBusinessCalendar to provide custom business calendar injection logic
 */
@Singleton
class CustomDurationBusinessCalendar(
        private val calendars: List<CustomBusinessCalendar>
) : BusinessCalendar, DurationBusinessCalendar(){

    override val calendarName: String = NAME

    private val LOG = ProcessEngineLogger.UTIL_LOGGER

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
                    println("try again Eval:" + initial)
                    resolveDuedate(duedate, Date.from(incrementer.invoke(initial.toInstant())))
                } else {
                    throw LOG.exceptionWhileResolvingDuedate(duedate, IllegalArgumentException("Duedate is invalid for provided business calendar."))
                }

            }

        } else {
            super.resolveDuedate(duedate, startDate)
        }
    }
}