package formsmanager.camunda.engine.businesscalendar

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessEngineLogger
import org.camunda.bpm.engine.impl.calendar.CycleBusinessCalendar
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton

/**
 * Extending the CycleBusinessCalendar to provide custom business calendar injection logic
 */
@Singleton
class CustomCycleBusinessCalendar(
        private val calendars: List<CustomBusinessCalendar>
) : BusinessCalendar, CycleBusinessCalendar() {

    override val calendarName: String = NAME

    private val LOG = ProcessEngineLogger.UTIL_LOGGER

    override fun resolveDuedate(duedateDescription: String, startDate: Date?, repeatOffset: Long): Date {
        val customCalendarDescription: CustomBusinessCalendar.Companion.CustomCalendarDescription? = CustomBusinessCalendar.getCustomCalendar(duedateDescription)

        return if (customCalendarDescription != null){
            val customCalendar = kotlin.runCatching {
                calendars.single { it.name == customCalendarDescription.calendarName }
            }.getOrElse {
                throw ProcessEngineException("Unable to find the custom calendar for the provided name: ${customCalendarDescription.calendarName}", it)
            }

            val initial = super.resolveDuedate(customCalendarDescription.duedateDescription, startDate, repeatOffset)

            val customCalendarEvalResult = customCalendar.evaluate(ZonedDateTime.ofInstant(initial.toInstant(), ZoneId.systemDefault()))

            return if (customCalendarEvalResult) {
                println("accepted Eval:" + initial)
                initial
            } else {
                println("try again Eval:" + initial)
                resolveDuedate(duedateDescription, initial, repeatOffset)
            }
        } else {
            super.resolveDuedate(duedateDescription, startDate, repeatOffset)
        }
    }
}