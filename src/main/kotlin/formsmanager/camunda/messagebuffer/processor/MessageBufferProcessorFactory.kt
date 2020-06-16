package formsmanager.camunda.messagebuffer.processor

import com.hazelcast.core.EntryEvent
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.listener.EntryAddedListener
import formsmanager.camunda.messagebuffer.MessageWrapper
import formsmanager.camunda.messagebuffer.repository.MessageBufferHazelcastRepository
import formsmanager.core.hazelcast.map.LocalEntryListener
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class MessageBufferProcessorFactory {

    companion object {
        /**
         * Name of the Camunda message buffer processor IExecutorService
         */
        const val MESSAGE_BUFFER_PROCESSOR = "camunda-message-buffer-processor"

        /**
         * Name of the Camunda message buffer processor retry service IScheduledExecutorService
         */
        const val MESSAGE_BUFFER_PROCESSOR_RETRY = "camunda-message-buffer-processor-retry"

    }

    @Singleton
    @Named("camunda-message-buffer-insert")
    @Context
    fun camundaMessageInsertListener(
            hazelcastInstance: HazelcastInstance,
            repository: MessageBufferHazelcastRepository
    ): LocalEntryListener {
        val listener = object: EntryAddedListener<String, MessageWrapper> {

            private val messageProcessorService = hazelcastInstance.getExecutorService(MESSAGE_BUFFER_PROCESSOR)

            override fun entryAdded(event: EntryEvent<String, MessageWrapper>) {
                messageProcessorService.submit(MessageProcessorTask(event.value.id.toMapKey()))
                //@TODO consider adding error handling here
            }
        }

        return LocalEntryListener(repository.iMap.addLocalEntryListener(listener))
    }
}