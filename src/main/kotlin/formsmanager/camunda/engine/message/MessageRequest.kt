package formsmanager.camunda.engine.message

data class MessageRequest(
        val name: String,
        val businessKey: String? = null,
        val tenantId: String? = null,
        val withoutTenantId: Boolean = false,
        val processInstanceId: String? = null,
        val processDefinitionId: String? = null,
        val correlationKeys: Map<String, Any?>? = null,
        val localCorrelationKeys: Map<String, Any?>? = null,
        val processVariables: Map<String, Any?>? = null,
        val processVariablesLocal: Map<String, Any?>? = null,
        val all: Boolean = false,
        val resultEnabled: Boolean = false,
        val variablesInResultEnabled: Boolean = false,
        val startMessagesOnly: Boolean = false

) {
    init {
        if (processInstanceId != null || processDefinitionId != null) {
            require(!withoutTenantId) {
                "withoutTenantId cannot be used if processInstanceId or processDefinitionId is used."
            }
        }
    }
}