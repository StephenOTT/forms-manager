package formsmanager.camunda.engine.businesscalendar

import org.camunda.bpm.engine.impl.calendar.BusinessCalendar

/**
 * Replacement BusinessCalendar interface that provides the calendar name as a field.
 * Inherits from the original Business Calendar interface
 */
interface BusinessCalendar: BusinessCalendar {
    val calendarName: String
}