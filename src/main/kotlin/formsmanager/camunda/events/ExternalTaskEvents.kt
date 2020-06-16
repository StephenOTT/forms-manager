package formsmanager.camunda.events

interface ExternalTaskEvent : CamundaReactiveEvent{
    val taskId: String
}

data class ExternalTaskCreated(
        override val taskId: String
) : ExternalTaskEvent

data class ExternalTaskUnlocked(
        override val taskId: String
) : ExternalTaskEvent