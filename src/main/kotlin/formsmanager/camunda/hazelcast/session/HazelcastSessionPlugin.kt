package formsmanager.camunda.hazelcast.session

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.SessionFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("hazelcast-session")
class HazelcastSessionPlugin(
        private val sessionFactories: List<SessionFactory>
) : MicronautProcessEnginePlugin {
    override fun getOrder(): Int {
        return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
    }

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (processEngineConfiguration.customSessionFactories == null) {
            processEngineConfiguration.customSessionFactories = mutableListOf()
        }
        processEngineConfiguration.customSessionFactories.addAll(sessionFactories)
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}