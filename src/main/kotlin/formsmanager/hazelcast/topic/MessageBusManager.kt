package formsmanager.hazelcast.topic

import com.hazelcast.topic.ITopic
import formsmanager.hazelcast.HazelcastJetManager
import formsmanager.hazelcast.task.TaskManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


/**
 * Generic Standard Message Bus for sending HazelcastTransportable based messages wrapped in a MessageWrapper
 */
@Singleton
class MessageBusManager(
        private val jet: HazelcastJetManager,
        private val taskManager: TaskManager
) {

    companion object {
        private val log = LoggerFactory.getLogger(MessageBusManager::class.java)
    }

    /**
     * Create a consumer for messages
     */
    fun <M : Any> consumer(address: String): Observable<MessageWrapper<M>> {
        val topic = jet.defaultInstance.getReliableTopic<MessageWrapper<M>>(address)
        return topic.createObservable()
    }

    fun <M : Any> destroy(address: String) {
        return jet.defaultInstance.getReliableTopic<MessageWrapper<M>>(address).destroy()
    }

    /**
     * Publish a message to a reliable topic address
     */
    fun <T : Any> publish(address: String, body: () -> MessageWrapper<T>): Completable {
        return Completable.fromAction {
            jet.defaultInstance.getReliableTopic<MessageWrapper<T>>(address)
                    .publish(body.invoke())
        }
    }

}

