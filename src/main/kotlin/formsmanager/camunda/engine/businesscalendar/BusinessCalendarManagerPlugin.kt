package formsmanager.camunda.engine.businesscalendar

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.calendar.BusinessCalendarManager
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("custom-business-calendar-manager")
@Requires(beans = [BusinessCalendarManager::class])
class BusinessCalendarManagerPlugin(
        private val businessCalendarManager: BusinessCalendarManager
): MicronautProcessEnginePlugin {
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