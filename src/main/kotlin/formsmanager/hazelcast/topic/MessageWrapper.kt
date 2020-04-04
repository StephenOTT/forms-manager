package formsmanager.hazelcast.topic

import formsmanager.validator.queue.HazelcastTransportable
import java.util.*

/**
 * Message wrapper for sending HazelcastTransportable messages, typically through a ITopic
 */
data class MessageWrapper<M : HazelcastTransportable>(
        val message: M,
        val correlationId: UUID = UUID.randomUUID(),
        val replyAddress: UUID? = null,
        val messageType: String? = null,
        val headers: Map<String, String> = mapOf()
) : HazelcastTransportable