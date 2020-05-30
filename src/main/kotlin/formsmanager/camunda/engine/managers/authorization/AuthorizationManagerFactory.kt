package formsmanager.camunda.engine.managers.authorization

import formsmanager.camunda.engine.managers.MicronautContextAwareGenericManagerReplacerFactory
import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager
import javax.inject.Named
import javax.inject.Singleton

@Factory
class AuthorizationManagerFactory {

    @Singleton
    @Named("custom-authorization-configurations")
    fun customAuthorizationConfigurations(appCtx: ApplicationContext): MicronautProcessEnginePlugin {
        return object : MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customSessionFactories == null) {
                    processEngineConfiguration.customSessionFactories = mutableListOf()
                }
                processEngineConfiguration.customSessionFactories.add(MicronautContextAwareGenericManagerReplacerFactory(AuthorizationManager::class.java, CustomAuthorizationManager::class.java, appCtx))
                processEngineConfiguration.isAuthorizationEnabled = true
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }
    }
}