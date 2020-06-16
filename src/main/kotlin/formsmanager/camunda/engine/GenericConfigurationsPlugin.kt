package formsmanager.camunda.engine

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

/**
 * A place to configure all of the generic Camunda Configurations that do not warrant a full plugin.
 */
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
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}