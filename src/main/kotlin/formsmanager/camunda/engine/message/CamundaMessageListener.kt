package formsmanager.camunda.engine.message

import com.hazelcast.core.EntryEvent
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.listener.EntryAddedListener
import formsmanager.camunda.engine.message.MessageBufferDataFactory.Companion.MESSAGE_BUFFER_PROCESSOR
import javax.inject.Singleton

@Singleton
class CamundaMessageListener(
        private val hazelcastInstance: HazelcastInstance
) : EntryAddedListener<String, MessageWrapper> {

    private val messageProcessorService = hazelcastInstance.getExecutorService(MESSAGE_BUFFER_PROCESSOR)

    override fun entryAdded(event: EntryEvent<String, MessageWrapper>) {
        messageProcessorService.submit(MessageProcessorTask(event.value.id.toMapKey()))
        //@TODO consider adding error handling here
    }
}