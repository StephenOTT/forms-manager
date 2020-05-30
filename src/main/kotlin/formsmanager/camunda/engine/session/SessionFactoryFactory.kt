package formsmanager.camunda.engine.session

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.SessionFactory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class SessionFactoryFactory {

    @Singleton
    @Named("custom-session-factories")
    fun customSessionFactories(sessionFactories: List<SessionFactory>, appCtx: ApplicationContext): MicronautProcessEnginePlugin {
        return object : MicronautProcessEnginePlugin {

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
    }
}