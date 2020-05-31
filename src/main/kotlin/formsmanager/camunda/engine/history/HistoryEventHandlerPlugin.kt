package formsmanager.camunda.engine.history

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("custom-history-handlers")
@Requires(property = "camunda.custom.history.handlers.enabled", value = StringUtils.TRUE)
class HistoryEventHandlerPlugin(
        private val historyEventHandlers: List<HistoryEventHandler>,
        private val historyEventHandlerCfg: HistoryEventHandlerConfiguration,
        private val compositeCfg: HistoryEventHandlerConfiguration.CompositeHistoryEventHandlerConfiguration
) : MicronautProcessEnginePlugin {

    override fun getOrder(): Int {
        return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
    }

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        //@TODO add logic with configuration on if to use Composite history or just use what is in the list.
        if (compositeCfg.enabled){
            if (compositeCfg.useDbComposite){
                processEngineConfiguration.historyEventHandler = CompositeDbHistoryEventHandler(historyEventHandlers)
            } else {
                processEngineConfiguration.historyEventHandler = CompositeHistoryEventHandler(historyEventHandlers)
            }
        }
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}