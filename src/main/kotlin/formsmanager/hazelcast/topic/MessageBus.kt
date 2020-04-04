package formsmanager.hazelcast.topic

import com.hazelcast.topic.ITopic
import com.hazelcast.topic.Message
import formsmanager.hazelcast.HazelcastJet
import formsmanager.validator.queue.HazelcastTransportable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Singleton

/**
 * Generic Standard Message Bus for sending HazelcastTransportable based messages wrapped in a MessageWrapper
 */
@Singleton
class StandardMessageBusManager(
        private val jet: HazelcastJet
) {

    val topics: ConcurrentMap<String, ITopic<MessageWrapper<HazelcastTransportable>>> = ConcurrentHashMap()

    /**s
     * Create a consume for a Reliable Topic address
     */
    fun <M: HazelcastTransportable> consumer(address: String, receiveAction: (message: Message<MessageWrapper<M>>) -> Unit) {
        (topics.computeIfAbsent(address) {
            jet.jet.getReliableTopic(address)
        } as ITopic<MessageWrapper<M>>) // @TODO Add check for bad casting / partial casting (deep objects)
                .addMessageListener {
                    receiveAction.invoke(it)
                }
    }

    /**
     * Publish a message to a reliable topic address
     */
    fun <T: HazelcastTransportable> publish(address: String, body: () -> MessageWrapper<T>) {
        (topics.computeIfAbsent(address) {
            jet.jet.getReliableTopic(address)
        } as ITopic<MessageWrapper<T>>)
                .publish(body.invoke())
    }

    /**
     * Publish a message to a reliable topic address which includes a response address and responseAction to invoke on receipt of the response.
     */
    fun <T: MessageWrapper<T>, R: HazelcastTransportable> request(address: String, body: () -> T, responseAction: (response: Message<MessageWrapper<R>>) -> Unit) {
        consumer(UUID.randomUUID().toString(), responseAction)
        publish(address, body)
    }

}

