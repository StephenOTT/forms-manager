package formsmanager.camunda.engine

import com.hazelcast.jet.JetInstance
import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration
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
    fun processEngineConfiguration(
            processEnginePlugins: List<MicronautProcessEnginePlugin>
    ): ProcessEngineConfiguration {
        //@TODO add configuration yml support
        return StandaloneProcessEngineConfiguration().apply {
            databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP
            this.jdbcUrl = "jdbc:h2:./build/DB/camunda-dbdevDb1;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
            this.processEnginePlugins.addAll(processEnginePlugins)
        }
    }

}


