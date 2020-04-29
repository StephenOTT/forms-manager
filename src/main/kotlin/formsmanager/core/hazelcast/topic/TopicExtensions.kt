package formsmanager.core.hazelcast.topic

import com.hazelcast.topic.ITopic
import io.reactivex.Observable
import java.util.*

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