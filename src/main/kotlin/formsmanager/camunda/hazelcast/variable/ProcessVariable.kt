package formsmanager.camunda.hazelcast.variable

import com.hazelcast.internal.util.UuidUtil
import java.time.Instant

/**
 * A process or local variable that is saved into Hazelcast.
 */
data class ProcessVariable(
        val mapKey: String = UuidUtil.newSecureUuidString(),
        val engine: String,
        val processInstanceId: String?,
        val processDefinitionId: String?,
        val processDefinitionKey: String?,
        val tenantId: String?,
        val createdAt: Instant?,
        val activityInstanceId: String?,
        val scope: String?,
        val variableName: String,
        val variableValue: Any?
)