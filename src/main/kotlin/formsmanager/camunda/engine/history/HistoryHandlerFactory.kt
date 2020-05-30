package formsmanager.camunda.engine.history

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import javax.inject.Named
import javax.inject.Singleton

@Factory
class HistoryHandlerFactory {

    @Singleton
    @Named("custom-history-handlers")
    fun historyEventHandlers(historyEventHandlers: List<HistoryEventHandler>): MicronautProcessEnginePlugin {
        return object : MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                //@TODO add logic with configuration on if to use Composite history or just use what is in the list.
                processEngineConfiguration.historyEventHandler = CompositeDbHistoryEventHandler(historyEventHandlers)
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }
    }
}