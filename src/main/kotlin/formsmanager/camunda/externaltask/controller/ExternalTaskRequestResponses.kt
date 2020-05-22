package formsmanager.camunda.externaltask.controller

import com.fasterxml.jackson.annotation.JsonValue
import formsmanager.camunda.externaltask.subscription.HttpExternalTaskSubscription
import org.camunda.bpm.engine.externaltask.LockedExternalTask

data class FetchAndLockRequest(
        @JsonValue val request: HttpExternalTaskSubscription
)

data class FetchAndLockResponse(
        @JsonValue val response: List<LockedExternalTask>
)

