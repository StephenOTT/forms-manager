package formsmanager.hazelcast.topic

import com.hazelcast.topic.ITopic
import formsmanager.hazelcast.HazelcastJetManager
import io.reactivex.Completable
import io.reactivex.Observable
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Singleton


/**
 * Generic Standard Message Bus for sending HazelcastTransportable based messages wrapped in a MessageWrapper
 */
@Singleton
class MessageBusManager(
        private val jet: HazelcastJetManager
) {

    companion object {
        private val log = LoggerFactory.getLogger(MessageBusManager::class.java)
    }

    val topics: ConcurrentMap<String, ITopic<MessageWrapper<Any>>> = ConcurrentHashMap()


    fun <M : Any> consumer(address: String): Observable<MessageWrapper<M>> {
        val topic = jet.defaultInstance.getReliableTopic<MessageWrapper<M>>(address)
        return topic.createObservable()
    }

    /**
     * Publish a message to a reliable topic address
     */
    fun <T : Any> publish(address: String, body: () -> MessageWrapper<T>): Completable {
        return Completable.fromAction{
                jet.defaultInstance.getReliableTopic<MessageWrapper<T>>(address)
                    .publish(body.invoke())
        }
    }

}

