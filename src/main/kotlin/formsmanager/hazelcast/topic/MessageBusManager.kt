package formsmanager.hazelcast.topic

import com.hazelcast.collection.IQueue
import com.hazelcast.collection.ItemEvent
import com.hazelcast.collection.ItemListener
import com.hazelcast.topic.ITopic
import com.hazelcast.topic.Message
import com.hazelcast.topic.ReliableMessageListener
import formsmanager.hazelcast.HazelcastJetManager
import io.reactivex.*
import io.reactivex.Observable
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import javax.inject.Singleton

inline fun <reified T: Any> ITopic<T>.createObservable(): Observable<T>{
    var listener: UUID? = null
    return Observable.create<T>{ emitter ->
        println("----->Setting up topic listener!! for ${this@createObservable.name}")
        listener = this.addMessageListener {
            emitter.onNext(it.messageObject)
        }
            //@TODO update hazelcast javadocs to indicate to use the ReliableMessageListener for iTopic
        require(listener != null, lazyMessage = {"Listener UUID was null...."})
    }.doOnDispose {
        println("----->DISPOSING")
        this.removeMessageListener(listener!!)
    }
}

inline fun <reified T: Any> IQueue<T>.createObservable(pollTimeout: Duration = Duration.ofSeconds(1)): Observable<T>{
    var listener: UUID? = null
    return Observable.create<T>{ emitter ->
        println("----->Setting up queue listener!!")
        listener = this.addItemListener(object: ItemListener<T>{
            override fun itemRemoved(item: ItemEvent<T>?) {
                // Do nothing
            }

            override fun itemAdded(item: ItemEvent<T>?) {
                val response = this@createObservable.poll(pollTimeout.seconds, TimeUnit.SECONDS)
                if (response != null) {
                    println("----->Got a Item from the poll")
                    emitter.onNext(response)
                } else {
                    throw IllegalStateException("Poll did not find any items in queue ${this@createObservable.name}")
                }
            }
        }, true)
        require(listener != null, lazyMessage = {"Listener UUID was null...."})

    }.doOnDispose {
        this.removeItemListener(listener!!)
    }
}


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


//    fun <M : Any> consumer(address: String): Observable<MessageWrapper<M>> {
//        val topic = (topics.computeIfAbsent(address) { jet.defaultInstance.getReliableTopic(address) } as ITopic<MessageWrapper<M>>) // @TODO Add check for bad casting / partial casting (deep objects)
//        return topic.createObservable()
//    }

    fun <M : Any> consumer(address: String): Observable<MessageWrapper<M>> {
        val topic = jet.defaultInstance.getReliableTopic<MessageWrapper<M>>(address) // @TODO Add check for bad casting / partial casting (deep objects)
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

//    fun <T : Any> publish(address: String, body: () -> MessageWrapper<T>): Completable {
//        return Completable.fromCallable {
//            (topics.computeIfAbsent(address) {
//                jet.defaultInstance.getReliableTopic(address)
//            } as ITopic<MessageWrapper<T>>)
//                    .publish(body.invoke())
//        }
//    }

//    /**
//     * Publish a message to a reliable topic address which includes a response address and responseAction to invoke on receipt of the response.
//     */
//    fun <T : MessageWrapper<T>, R : Any> request(address: String, ttl: Duration?, body: () -> T): CompletableFuture<Message<MessageWrapper<R>>> {
//        consumer<R>(UUID.randomUUID().toString(), ttl)
//        return publish(address, body)
//    }

}

