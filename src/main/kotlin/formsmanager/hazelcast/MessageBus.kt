package formsmanager.hazelcast

import com.hazelcast.topic.ITopic
import com.hazelcast.topic.Message
import formsmanager.validator.queue.HazelcastTransportable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Singleton

/**
 * Message wrapper for sending HazelcastTransportable messages, typically through a ITopic
 */
data class MessageWrapper<M : HazelcastTransportable>(
        val correlationId: UUID = UUID.randomUUID(),
        val replyAddress: UUID? = null,
        val messageType: String? = null,
        val headers: Map<String, String> = mapOf(),
        val message: M
) : HazelcastTransportable

/**
 * Generic Standard Message Bus for sending HazelcastTransportable based messages wrapped in a MessageWrapper
 */
@Singleton
class StandardMessageBusManager(
        private val jet: HazelcastJet
) {

    val topics: ConcurrentMap<String, ITopic<MessageWrapper<out HazelcastTransportable>>> = ConcurrentHashMap()

    /**s
     * Create a consume for a Reliable Topic address
     */
    fun <M: HazelcastTransportable> consumer(address: String, receiveAction: (message: Message<MessageWrapper<M>>) -> Unit) {
        (topics.computeIfAbsent(address) {
            jet.jet.getReliableTopic(address)
        } as ITopic<MessageWrapper<M>>)
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

