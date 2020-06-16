package formsmanager.camunda.messagebuffer

import java.time.Instant

/**
 * Used to store the correlation details in the Message Buffer objects
 */
data class CorrelationResult(
        val correlatedOn: Instant,
        val type: String,
        val processInstanceId: String,
        val executionId: String?
)