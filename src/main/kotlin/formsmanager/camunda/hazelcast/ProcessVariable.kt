package formsmanager.camunda.hazelcast

import com.hazelcast.internal.util.UuidUtil

data class ProcessVariable(
        val variableId: String = UuidUtil.newSecureUuidString(),
        val engine: String,
        val processInstanceId: String?,
        val tenantId: String?,
        val variableName: String,
        val variableValue: Any?
)