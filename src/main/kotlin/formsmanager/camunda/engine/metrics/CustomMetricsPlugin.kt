package formsmanager.camunda.engine.metrics

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.metrics.reporter.DbMetricsReporter
import javax.inject.Singleton

@Singleton
class CustomMetricsPlugin(
        private val camundaMetricsConfiguration: CamundaMetricsConfiguration
) : MicronautProcessEnginePlugin {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        val metricsReporter = DbMetricsReporter(
                processEngineConfiguration.metricsRegistry,
                processEngineConfiguration.commandExecutorTxRequired
        )
        metricsReporter.reportingIntervalInSeconds = camundaMetricsConfiguration.reportingInterval

        processEngineConfiguration.dbMetricsReporter = metricsReporter;
    }
}