package formsmanager.camunda.engine.message.persistence

import formsmanager.core.hazelcast.map.CrudableObjectId
import java.util.*


data class MessageId(override val value: UUID): CrudableObjectId<MessageId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun type(): String {
        return "message-id"
    }

    override fun compareTo(other: MessageId): Int {
        return value.compareTo(other.value)
    }
}