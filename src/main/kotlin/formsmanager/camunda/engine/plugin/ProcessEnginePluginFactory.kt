package formsmanager.camunda.engine.plugin

import formsmanager.camunda.engine.managers.MicronautContextAwareGenericManagerReplacerFactory
import formsmanager.camunda.engine.managers.CustomAuthorizationManager
import formsmanager.camunda.engine.managers.CustomExternalTaskManager
import formsmanager.camunda.engine.services.CustomExternalTaskServiceImpl
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import org.camunda.bpm.engine.impl.interceptor.SessionFactory
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer
import javax.inject.Named
import javax.inject.Singleton

@Factory
class ProcessEnginePluginFactory {

    @Singleton
    @Named("custom-job-executor")
    fun jobExecutorPlugin(jobExecutor: JobExecutor): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

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
    }

    @Singleton
    @Named("custom-session-factories")
    fun customSessionFactories(sessionFactories: List<SessionFactory>, appCtx: ApplicationContext): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customSessionFactories == null){
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

    @Singleton
    @Named("custom-external-task-service")
    fun externalTaskService(externalTaskService: CustomExternalTaskServiceImpl, appCtx: ApplicationContext): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customSessionFactories == null){
                    processEngineConfiguration.customSessionFactories = mutableListOf()
                }
                processEngineConfiguration.customSessionFactories.add(MicronautContextAwareGenericManagerReplacerFactory(ExternalTaskManager::class.java, CustomExternalTaskManager::class.java, appCtx))
                processEngineConfiguration.externalTaskService = externalTaskService
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {}

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {}
        }
    }

    @Singleton
    @Named("custom-variable-serializers")
    fun externalTaskService(variableSerializers: List<AbstractTypedValueSerializer<*>>): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customPostVariableSerializers == null){
                    processEngineConfiguration.customPostVariableSerializers = mutableListOf()
                }
                processEngineConfiguration.customPostVariableSerializers.addAll(variableSerializers)
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }
    }

    @Singleton
    @Named("custom-history-handlers")
    fun historyEventHandlers(historyEventHandlers: List<HistoryEventHandler>): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                //@TODO add logic with configuration on if to use Composite history or just use what is in the list.
                processEngineConfiguration.historyEventHandler = CompositeDbHistoryEventHandler(historyEventHandlers)
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }
    }

    @Singleton
    @Named("custom-authorization-configurations")
    fun customAuthorizationConfigurations(appCtx: ApplicationContext): MicronautProcessEnginePlugin {
        return object: MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customSessionFactories == null){
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



