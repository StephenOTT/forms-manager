package formsmanager.camunda.externaltask

import com.fasterxml.jackson.annotation.JsonValue
import org.camunda.bpm.engine.externaltask.LockedExternalTask

data class FetchAndLockRequest(
        @JsonValue val request: HttpExternalTaskSubscription
)

data class FetchAndLockResponse(
        @JsonValue val response: List<LockedExternalTask>
)

