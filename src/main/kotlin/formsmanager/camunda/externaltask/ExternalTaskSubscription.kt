package formsmanager.camunda.externaltask

import io.reactivex.Observable
import io.reactivex.subjects.AsyncSubject
import java.util.concurrent.Future

interface ExternalTaskSubscription {
    val workerId: String
    val maxTasks: Int
    val usePriority: Boolean
    val asyncResponseTimeout: Long
    val topics: List<TopicDefinition>

    var requestFuture: Future<Unit>

    val subject: AsyncSubject<FetchAndLockResponse>

    fun provideResponse(response: FetchAndLockResponse) {
        subject.onNext(response)
        subject.onComplete()
    }

    fun observable(): Observable<FetchAndLockResponse> {
        return subject
    }
}