package formsmanager.camunda.engine.message

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.hazelcast.LocalEntryListener
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class MessageBufferDataFactory {

    companion object {
        /**
         * Name of the camunda message buffer processor IExecutorService
         */
        const val MESSAGE_BUFFER_PROCESSOR = "camunda-message-buffer-processor"

        /**
         * Name of the camunda message buffer processor retry service IScheduledExecutorService
         */
        const val MESSAGE_BUFFER_PROCESSOR_RETRY = "camunda-message-buffer-processor-retry"

    }

    @Singleton
    @Named(CamundaMessageBuffer.BUFFER_NAME)
    fun messageBufferMap(hazelcastInstance: HazelcastInstance): IMap<String, MessageWrapper> {
        return hazelcastInstance.getMap(CamundaMessageBuffer.BUFFER_NAME)
    }

    @Singleton
    @Named("camunda-message-insert")
    @Context
    fun camundaMessageInsertListener(
            @Named(CamundaMessageBuffer.BUFFER_NAME) messages: IMap<String, MessageWrapper>,
            listener: CamundaMessageListener): LocalEntryListener {
        return LocalEntryListener(messages.addLocalEntryListener(listener))
    }

//    @Singleton
//    @Named(MESSAGE_BUFFER_PROCESSOR_RETRY)
//    fun messageScheduledExecutorService(hazelcastInstance: HazelcastInstance): IScheduledExecutorService {
//        return hazelcastInstance.getScheduledExecutorService(MESSAGE_BUFFER_PROCESSOR_RETRY)
//    }
//
//    @Singleton
//    @Named(MESSAGE_BUFFER_PROCESSOR)
//    fun messageExecutorService(hazelcastInstance: HazelcastInstance): IExecutorService {
//        return hazelcastInstance.getExecutorService(MESSAGE_BUFFER_PROCESSOR)
//    }


}