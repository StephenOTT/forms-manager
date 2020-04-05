package formsmanager.validator.queue

import formsmanager.hazelcast.HazelcastJetManager
import formsmanager.hazelcast.annotation.HazelcastJet
import formsmanager.hazelcast.annotation.QueueConsumer
import formsmanager.hazelcast.queue.ItemWrapper
import formsmanager.hazelcast.topic.StandardMessageBusManager
import formsmanager.validator.FormSubmission
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.discovery.event.ServiceStartedEvent
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@HazelcastJet
class ValidatorProcessorConsumer {

    @QueueConsumer("form-schemas-validator")
    fun myQueue1(task: ItemWrapper<FormSubmission>) {
        println("dogs!!!!")
    }
}

@Singleton
class ValidatorProcessor(
        private val mb: StandardMessageBusManager,
        private val jet: HazelcastJetManager
) : ApplicationEventListener<ServiceStartedEvent> {

    companion object {
        private val log = LoggerFactory.getLogger(ValidatorProcessor::class.java)
    }

    override fun onApplicationEvent(event: ServiceStartedEvent?) {
        val queue = jet.defaultInstance.hazelcastInstance.getQueue<ItemWrapper<FormSubmission>>("form-schemas-validator")

        println("setting up consumer!!")
        mb.consumer<FormSubmission>("form-submission-validation") {
            println("---->Consume start")
            println(it.messageObject::class.qualifiedName)
            println(it.messageObject.message.schema.display)
            println("---->Consume done")
        }
    }
}