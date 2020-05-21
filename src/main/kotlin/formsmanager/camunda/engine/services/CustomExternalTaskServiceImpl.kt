package formsmanager.camunda.engine.services

import formsmanager.camunda.events.CamundaReactiveEvents
import formsmanager.camunda.events.ExternalTaskUnlocked
import org.camunda.bpm.engine.impl.ExternalTaskServiceImpl
import javax.inject.Singleton

@Singleton
class CustomExternalTaskServiceImpl(
        private val events: CamundaReactiveEvents
) : ExternalTaskServiceImpl(){

    private val externalTasksEvents = events.externalTaskEvents

    override fun unlock(externalTaskId: String) {
        return kotlin.runCatching {
            super.unlock(externalTaskId)
        }.onSuccess {
            externalTasksEvents.onNext(ExternalTaskUnlocked(externalTaskId))
        }.getOrThrow()
    }
}