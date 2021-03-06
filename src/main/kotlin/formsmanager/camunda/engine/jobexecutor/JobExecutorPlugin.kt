package formsmanager.camunda.engine.jobexecutor

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("custom-job-executor")
class JobExecutorPlugin(
        private val jobExecutor: JobExecutor
) : MicronautProcessEnginePlugin {
    override fun getOrder(): Int {
        return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
    }

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        processEngineConfiguration.jobExecutor = jobExecutor
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}