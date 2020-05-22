package formsmanager.camunda.externaltask

import io.micronaut.core.annotation.Introspected
import io.reactivex.subjects.AsyncSubject
import net.minidev.json.annotate.JsonIgnore
import java.util.concurrent.Future

@Introspected
data class HttpExternalTaskSubscription(
        override val workerId: String,
        override val maxTasks: Int = 1,
        override val usePriority: Boolean = true,
        override val asyncResponseTimeout: Long = 1800000, // 30 min
        override val topics: List<TopicDefinition>
) : ExternalTaskSubscription {

    @JsonIgnore
    override lateinit var requestFuture: Future<Unit>

    @JsonIgnore
    override val subject = AsyncSubject.create<FetchAndLockResponse>()
}