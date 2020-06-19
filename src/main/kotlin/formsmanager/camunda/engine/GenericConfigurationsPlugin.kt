package formsmanager.camunda.engine

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import javax.inject.Named
import javax.inject.Singleton

/**
 * A place to configure all of the generic Camunda Configurations that do not warrant a full plugin.
 */
@Singleton
@Named("generic-configurations")
class GenericConfigurationsPlugin(
        private val camundaConfiguration: CamundaConfiguration,
        private val bpmConfiguration: CamundaConfiguration.Bpm,
        private val jobExecutorConfiguration: CamundaConfiguration.Bpm.JobExecutor,
        private val authorizationConfiguration: CamundaConfiguration.Bpm.Authorization,
        private val databaseConfiguration: CamundaConfiguration.Bpm.Database,
        private val metricsConfiguration: CamundaConfiguration.Bpm.Metrics

) : MicronautProcessEnginePlugin {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        bpmConfiguration.bpmnStacktraceVerbose?.let {
            processEngineConfiguration.isBpmnStacktraceVerbose = it
        }

        bpmConfiguration.historyLevel.let {
            processEngineConfiguration.setHistory(it)
        }

    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {

    }
}