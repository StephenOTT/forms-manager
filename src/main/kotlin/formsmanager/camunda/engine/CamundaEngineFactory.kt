package formsmanager.camunda.engine

import com.hazelcast.jet.JetInstance
import formsmanager.camunda.engine.managers.CustomExternalTaskManager
import formsmanager.camunda.engine.services.CustomExternalTaskServiceImpl
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager
import javax.inject.Singleton

@Factory
class CamundaEngineFactory {

    @Singleton
    @Primary
    @Context
    @Requires(beans = [JetInstance::class])
    fun processEngine(configuration: ProcessEngineConfiguration): ProcessEngine {
        val engine = configuration.buildProcessEngine()
        println("----> Started Engine: ${engine.name}")
        return engine
    }

    @Singleton
    @Primary
    fun processEngineConfiguration(appCtx: ApplicationContext,
                                    externalTaskService: CustomExternalTaskServiceImpl): ProcessEngineConfiguration {
        //@TODO add configuration yml support
        return StandaloneInMemProcessEngineConfiguration().apply {
            this.jdbcUrl = "jdbc:h2:./build/DB/camunda-dbdevDb1;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
            if (this.customSessionFactories == null){
                this.customSessionFactories = mutableListOf()
            }
            this.customSessionFactories.add(MicronautContextAwareGenericManagerReplacerFactory(ExternalTaskManager::class.java, CustomExternalTaskManager::class.java, appCtx))
            this.externalTaskService = externalTaskService
        }
    }

}


