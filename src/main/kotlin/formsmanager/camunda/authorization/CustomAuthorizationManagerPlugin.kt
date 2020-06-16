package formsmanager.camunda.authorization

import formsmanager.camunda.engine.managers.MicronautContextAwareGenericManagerReplacerFactory
import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("custom-authorization-configurations")
@Requirements(
        Requires(beans = [ProcessEngine::class]),
        Requires(property = "camunda.custom.authorization", value = StringUtils.TRUE)
)
class CustomAuthorizationManagerPlugin(
        private val appCtx: ApplicationContext
) : MicronautProcessEnginePlugin {

    override fun getOrder(): Int {
        return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
    }

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (processEngineConfiguration.customSessionFactories == null) {
            processEngineConfiguration.customSessionFactories = mutableListOf()
        }
        processEngineConfiguration.customSessionFactories.add(
                MicronautContextAwareGenericManagerReplacerFactory(
                        AuthorizationManager::class.java,
                        CustomAuthorizationManager::class.java, appCtx
                ))

        processEngineConfiguration.isAuthorizationEnabled = true
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}