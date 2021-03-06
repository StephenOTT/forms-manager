package formsmanager.camunda.messagebuffer

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.hazelcast.map.CrudableObjectId
import io.micronaut.core.annotation.Introspected
import java.time.Instant
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


@Introspected
data class MessageWrapper(
        val message: MessageRequest,
        var state: State = State.CORRELATING,
        val insertAt: Instant = Instant.now(),
        var lastCorrelatedOn: Instant? = null,
        var correlatedWith: MutableList<CorrelationResult> = mutableListOf(),
        var attemptsCount: Int = 0,
        val attempts: MutableList<Instant> = mutableListOf(),
        val id: MessageId = MessageId(UuidUtil.newSecureUUID())
) {

    enum class State {
        CORRELATING, PAUSED, EXPIRED, CORRELATED
    }


    /**
     * Used to store the correlation details in the Message Buffer objects
     */
    data class CorrelationResult(
            val correlatedOn: Instant,
            val type: String,
            val processInstanceId: String,
            val executionId: String?
    )


    fun expire() {
        this.state = State.EXPIRED
    }

    fun correlated() {
        this.state = State.CORRELATED
    }

    fun addAttempt(attemptOn: Instant) {
        this.attempts.add(attemptOn)
        this.attemptsCount = this.attemptsCount + 1
    }

    fun addCorrelation(correlatedOn: Instant, correlatedWith: List<CorrelationResult>) {
        this.lastCorrelatedOn = correlatedOn
        this.correlatedWith.addAll(correlatedWith)
    }
}