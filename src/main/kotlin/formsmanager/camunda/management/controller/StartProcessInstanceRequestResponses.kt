package formsmanager.camunda.management.controller

import io.micronaut.core.annotation.Introspected

@Introspected
data class StartProcessInstanceRequest(
    val id: String? = null,
    val key: String? = null,
    val businessKey: String? = null,
    val variables: Map<String, Any?> = mapOf()
){
    init {
        require(id == null || key == null){
            "Cannot provide both 'id' and 'key'"
        }

        require(id != null || key != null){
            "Properties 'id' or 'key' must have a value."
        }

        businessKey?.let {
            require(it.length <= 255){
                "The `businessKey` must be less than or equal to 255 characters"
            }
        }

        variables?.let {
            require(variables.keys.all {
                it.length <= 200 &&
                variableNameRegex.matches(it)
            }){ "Invalid variable name: must be less than 200 characters, and match `^[a-zA-Z_\$][a-zA-Z0-9_\$]*` " }
        }

    }

    companion object{
        val variableNameRegex = Regex("^[a-zA-Z_$][a-zA-Z0-9_$]*")
    }
}