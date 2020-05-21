package formsmanager.camunda.externaltask

import io.micronaut.core.annotation.Introspected
import io.netty.channel.ChannelFuture
import io.reactivex.Observable
import io.reactivex.subjects.AsyncSubject
import net.minidev.json.annotate.JsonIgnore

interface ExternalTaskSubscription {
    val workerId: String
    val maxTasks: Int
    val usePriority: Boolean
    val asyncResponseTimeout: Long
    val topics: List<TopicDefinition>

    @get:JsonIgnore
    val subject: AsyncSubject<FetchAndLockResponse>

    @JsonIgnore
    fun provideResponse(response: FetchAndLockResponse) {
        subject.onNext(response)
        subject.onComplete()
    }

    @JsonIgnore
    fun observable(): Observable<FetchAndLockResponse> {
        return subject
    }

}

interface HttpBasedExternalTaskSubscription : ExternalTaskSubscription {
    var closeFuture: ChannelFuture
}


@Introspected
data class HttpExternalTaskSubscription(
        override val workerId: String,
        override val maxTasks: Int = 1,
        override val usePriority: Boolean = true,
        override val asyncResponseTimeout: Long = 1800000, // 30 min
        override val topics: List<TopicDefinition>
) : HttpBasedExternalTaskSubscription {

    override lateinit var closeFuture: ChannelFuture

    @get:JsonIgnore
    override val subject = AsyncSubject.create<FetchAndLockResponse>()
}