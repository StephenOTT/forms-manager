package formsmanager.core.hazelcast.topic

import com.hazelcast.topic.ITopic
import io.reactivex.Observable
import java.util.*

/**
 * Reactive Kotlin Extension for creating a reactive Observable.
 * On creation of the observable it will create a message listener against the topic.
 * On Dispose of the Observable, the Message Listener will be removed.
 */
fun <T: Any> ITopic<T>.createObservable(): Observable<T> {
    var listener: UUID? = null

    return Observable.create<T>{ emitter ->

        listener = this.addMessageListener {
            emitter.onNext(it.messageObject)
        }

        require(listener != null, lazyMessage = {"Listener UUID was null...."})

    }.doOnDispose {
        this.removeMessageListener(listener!!)
    }
}