package formsmanager.camunda.engine.businesscalendar

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.impl.calendar.BusinessCalendarManager
import org.camunda.bpm.engine.impl.calendar.MapBusinessCalendarManager
import javax.inject.Named
import javax.inject.Singleton


@Factory
class BusinessCalendarManagerFactory {

    @Singleton
    @Primary
    @Named("default")
    @Requires(beans = [BusinessCalendar::class])
    fun businessCalendarManager(businessCalendars: List<BusinessCalendar>): BusinessCalendarManager {
        val manager = MapBusinessCalendarManager()
        businessCalendars.forEach {
            manager.addBusinessCalendar(it.calendarName, it)
        }
        return manager
    }
}