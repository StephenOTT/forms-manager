package formsmanager.camunda.engine.deploymentcache

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.deploy.cache.CacheFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("deployment-cache-factory")
@Requires(beans = [CacheFactory::class])
class DeploymentCacheFactoryPlugin(
        private val cacheFactory: CacheFactory
) : MicronautProcessEnginePlugin {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        processEngineConfiguration.setCacheFactory(cacheFactory)
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun getOrder(): Int {
        return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
    }
}