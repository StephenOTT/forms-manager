package formsmanager.camunda.externaltask

import io.micronaut.core.annotation.Introspected

/**
 * Used by [ExternalTaskSubscription] for definition of Topics in a Long Poll Subscription.
 */
@Introspected
data class TopicDefinition(
        val topicName: String,
        val lockDuration: Long = 60000L,
        val withoutTenantId: Boolean? = null,
        val tenantIdIn: List<String>? = null,
        val businessKey: String? = null,
        val processDefinitionIdIn: List<String>? = null,
        val processDefinitionKeyIn: List<String>? = null,
        val processDefinitionVersionTag: String? = null,
        val variables: List<String>? = null,
        val processVariables: Map<String, Any?>? = null,
        val deserializeValues: Boolean = false,
        val localVariables: Boolean = false
)