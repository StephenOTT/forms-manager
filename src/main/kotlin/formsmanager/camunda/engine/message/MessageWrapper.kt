package formsmanager.camunda.engine.message

import com.hazelcast.internal.util.UuidUtil
import formsmanager.camunda.engine.message.persistence.MessageId
import io.micronaut.core.annotation.Introspected
import java.time.Instant

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