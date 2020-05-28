package formsmanager.camunda.engine.jobexecutor

import formsmanager.camunda.engine.CamundaConfiguration
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import javax.inject.Named
import javax.inject.Singleton

@Factory
class MicronautJobExecutor() : DefaultJobExecutor() {

    @Singleton
    @Primary
    @Named("default")
    fun jobExecutor(properties: CamundaConfiguration.Bpm.JobExecutor): JobExecutor{
        val executor = DefaultJobExecutor()

        properties.corePoolSize?.let {
            executor.corePoolSize = it
        }

        properties.maxPoolSize?.let {
            executor.maxPoolSize = it
        }

        properties.queueSize?.let {
            executor.queueSize = it
        }

        return executor
    }
}