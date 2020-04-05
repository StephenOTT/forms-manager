package formsmanager.hazelcast.queue

import com.hazelcast.collection.IQueue
import com.hazelcast.collection.ItemEvent
import com.hazelcast.collection.ItemListener
import formsmanager.hazelcast.HazelcastJetManager
import formsmanager.hazelcast.HazelcastTransportable
import io.micronaut.context.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 *
 */
@Singleton
class QueueManager(
        private val jet: HazelcastJetManager,
        private val applicationContext: ApplicationContext
) {

    val queues: ConcurrentMap<String, IQueue<ItemWrapper<HazelcastTransportable>>> = ConcurrentHashMap()

    fun <I: HazelcastTransportable> consumer(clazz: KClass<I>, qName: String, takeAction: (message: ItemWrapper<I>) -> Unit){
        consumer(qName, takeAction)
    }

    /**s
     * Create a consume for a Reliable Topic address
     */
    fun <I: HazelcastTransportable> consumer(qName: String, takeAction: (message: ItemWrapper<I>) -> Unit) {
        val queue = (queues.computeIfAbsent(qName) {
            jet.defaultInstance.hazelcastInstance.getQueue(qName)
        } as IQueue<ItemWrapper<I>>) // @TODO Add check for bad casting / partial casting (deep objects)

        queue.addItemListener(object : ItemListener<ItemWrapper<I>>{
                    override fun itemRemoved(item: ItemEvent<ItemWrapper<I>>?) {
                        //do nothing
                    }

                    override fun itemAdded(item: ItemEvent<ItemWrapper<I>>?) {
                        //When a item is received it will poll for that item.
                        queue.poll(2L, TimeUnit.SECONDS)?.let {
                            takeAction.invoke(it)
                        }
                    }
                }, true) //@TODO Review true value usage
    }

    /**
     * Publish a message to a reliable topic address
     * Defaults to a 2second timeout for offering/inserting into queue.
     */
    fun <I: HazelcastTransportable> publish(queue: String, body: () -> ItemWrapper<I>, timeout: Long = 2L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        (queues.computeIfAbsent(queue) {
            jet.defaultInstance.hazelcastInstance.getQueue(queue)
        } as IQueue<ItemWrapper<I>>).offer(body.invoke(), timeout, timeUnit)
    }
}

