package formsmanager.camunda.externaltask

import formsmanager.camunda.engine.managers.MicronautContextAwareGenericManagerReplacerFactory
import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.ApplicationContext
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("custom-external-task-service")
class ExternalTaskReactivePlugin(
        private val externalTaskService: CustomExternalTaskServiceImpl,
        private val appCtx: ApplicationContext): MicronautProcessEnginePlugin {

        override fun getOrder(): Int {
            return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
        }

        override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            if (processEngineConfiguration.customSessionFactories == null) {
                processEngineConfiguration.customSessionFactories = mutableListOf()
            }
            processEngineConfiguration.customSessionFactories.add(
                    MicronautContextAwareGenericManagerReplacerFactory(
                            ExternalTaskManager::class.java,
                            CustomExternalTaskManager::class.java, appCtx
                    ))

            processEngineConfiguration.externalTaskService = externalTaskService
        }

        override fun postProcessEngineBuild(processEngine: ProcessEngine) {}

        override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {}
}