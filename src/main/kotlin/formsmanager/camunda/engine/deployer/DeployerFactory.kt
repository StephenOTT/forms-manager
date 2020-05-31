package formsmanager.camunda.engine.deployer

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("custom-deployers")
@Requires(beans = [Deployer::class])
class DeployersPlugin(
        private val deployers: List<Deployer>
) : MicronautProcessEnginePlugin {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (processEngineConfiguration.customPreDeployers == null) {
            processEngineConfiguration.customPreDeployers = mutableListOf()
        }

        if (processEngineConfiguration.customPostDeployers == null) {
            processEngineConfiguration.customPostDeployers = mutableListOf()
        }

        deployers.forEach {
            when (it) {
                is PreDeploy -> {
                    processEngineConfiguration.customPreDeployers.add(it)
                }
                is PostDeploy -> {
                    processEngineConfiguration.customPostDeployers.add(it)
                }
                else -> {
                    // Default is Pre-Deploy
                    processEngineConfiguration.customPreDeployers.add(it)
                }
            }
        }
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}