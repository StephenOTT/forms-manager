package formsmanager.camunda.engine.jobexecutor

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import javax.inject.Singleton

@Singleton
class CamundaJobExecutorStartupListener(
        private val jobExecutor: JobExecutor
): ApplicationEventListener<StartupEvent> {

    override fun onApplicationEvent(event: StartupEvent) {
        if (!jobExecutor.isActive) {
            println("----->starting job executor for camunda using micronaut-camunda job executor")
            jobExecutor.start()
        }
    }
}