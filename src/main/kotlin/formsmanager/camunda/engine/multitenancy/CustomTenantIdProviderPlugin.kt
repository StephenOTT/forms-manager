package formsmanager.camunda.engine.multitenancy

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider
import javax.inject.Singleton

@Singleton
@Requirements(
        Requires(beans = [ProcessEngine::class, TenantIdProvider::class])
)
class CustomTenantIdProviderPlugin(
        private val tenantIdProvider: TenantIdProvider
) : MicronautProcessEnginePlugin {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        processEngineConfiguration.tenantIdProvider = tenantIdProvider
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}