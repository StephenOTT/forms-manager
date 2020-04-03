package formsmanager.domain

import formsmanager.validator.queue.HazelcastTransportable

data class FormSchema(
        val display: String,
        val components: List<Map<String, Any>>,
        val settings: Map<String, Any>? = null
) : HazelcastTransportable {}