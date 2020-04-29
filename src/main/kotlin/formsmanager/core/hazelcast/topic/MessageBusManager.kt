package formsmanager.core.hazelcast.topic

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.task.TaskManager
import io.reactivex.Completable
import io.reactivex.Observable
import org.slf4j.LoggerFactory
import javax.inject.Singleton


/**
 * Generic Standard Message Bus for sending HazelcastTransportable based messages wrapped in a MessageWrapper
 */
@Singleton
class MessageBusManager(
        private val hazelcastInstance: HazelcastInstance,
        private val taskManager: TaskManager
) {

    companion object {
        private val log = LoggerFactory.getLogger(MessageBusManager::class.java)
    }

    /**
     * Create a consumer for messages
     */
    fun <M : Any> consumer(address: String): Observable<MessageWrapper<M>> {
        val topic = hazelcastInstance.getReliableTopic<MessageWrapper<M>>(address)
        return topic.createObservable()
    }

    fun <M : Any> destroy(address: String) {
        return hazelcastInstance.getReliableTopic<MessageWrapper<M>>(address).destroy()
    }

    /**
     * Publish a message to a reliable topic address
     */
    fun <T : Any> publish(address: String, body: () -> MessageWrapper<T>): Completable {
        return Completable.fromAction {
            hazelcastInstance.getReliableTopic<MessageWrapper<T>>(address)
                    .publish(body.invoke())
        }
    }

}

