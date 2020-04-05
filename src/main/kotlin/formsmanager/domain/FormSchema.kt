package formsmanager.domain

import formsmanager.hazelcast.HazelcastTransportable

data class FormSchema(
        val display: String,
        val components: List<Map<String, Any>>,
        val settings: Map<String, Any>? = null
) : HazelcastTransportable {}