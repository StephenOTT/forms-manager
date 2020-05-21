package formsmanager.camunda.events

import org.camunda.bpm.engine.externaltask.ExternalTask

interface ExternalTaskEvent : CamundaEvent

data class ExternalTaskCreated(
        val task: ExternalTask
) : ExternalTaskEvent

data class ExternalTaskUnlocked(
        val taskId: String
) : ExternalTaskEvent