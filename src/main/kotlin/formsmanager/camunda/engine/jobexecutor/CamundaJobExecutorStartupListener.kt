package formsmanager.camunda.engine.jobexecutor

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import javax.inject.Singleton

@Singleton
class CamundaJobExecutorStartupListener(
        private val jobExecutor: JobExecutor
): ApplicationEventListener<ServerStartupEvent> {

    override fun onApplicationEvent(event: ServerStartupEvent) {
        if (!jobExecutor.isActive) {
            println("starting job executor for camunda using micronaut-camunda job executor")
            jobExecutor.start()
        }
    }
}