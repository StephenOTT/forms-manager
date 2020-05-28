package formsmanager.camunda.engine.variable

import com.hazelcast.internal.util.UuidUtil
import java.time.Instant

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