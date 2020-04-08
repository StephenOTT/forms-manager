package formsmanager.hazelcast.topic

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

/**
 * Message wrapper for sending HazelcastTransportable messages, typically through a ITopic
 */
data class MessageWrapper<M: Any>(
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "messageType")
        val message: M,
        val correlationId: UUID = UUID.randomUUID(),
        val replyAddress: String? = null,
        val messageType: String = message::class.qualifiedName!!,
        val headers: Map<String, String> = mapOf()
)