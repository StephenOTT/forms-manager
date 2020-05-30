package formsmanager.camunda.engine.managers

import formsmanager.camunda.engine.businesscalendar.BusinessCalendar
import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.calendar.BusinessCalendarManager
import org.camunda.bpm.engine.impl.calendar.MapBusinessCalendarManager
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import javax.inject.Named
import javax.inject.Singleton


@Factory
class BusinessCalendarManagerFactory {

    @Singleton
    @Primary
    @Named("default")
    fun businessCalendarManager(businessCalendars: List<BusinessCalendar>): BusinessCalendarManager {
        val manager = MapBusinessCalendarManager()
        businessCalendars.forEach {
            manager.addBusinessCalendar(it.calendarName, it)
        }
        return manager
    }

    @Singleton
    @Named("custom-business-calendar-manager")
    fun customBusinessCalendarManagerPlugin(businessCalendarManager: BusinessCalendarManager): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                processEngineConfiguration.businessCalendarManager = businessCalendarManager
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }
    }
}