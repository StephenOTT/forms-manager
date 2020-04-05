package formsmanager.hazelcast.queue

import formsmanager.hazelcast.HazelcastTransportable
import java.util.*

data class ItemWrapper<I : HazelcastTransportable>(
        val item: I,
        val correlationId: UUID = UUID.randomUUID(),
        val replyAddress: UUID? = null,
        val taskType: String? = null,
        val headers: Map<String, String> = mapOf()
) : HazelcastTransportable