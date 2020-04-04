package formsmanager.validator.queue

import formsmanager.hazelcast.annotation.Hazelcast
import formsmanager.hazelcast.HazelcastJet
import formsmanager.hazelcast.topic.StandardMessageBusManager
import formsmanager.hazelcast.annotation.QueueConsumer
import formsmanager.hazelcast.topic.MessageWrapper
import formsmanager.ifDebugEnabled
import formsmanager.validator.FormSubmission
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.discovery.event.ServiceStartedEvent
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Hazelcast
class ValidatorProcessorConsumer {

    @QueueConsumer("form-schemas-validator")
    fun myQueue1(task: TaskWrapper<FormSubmission>) {
        println("dogs!!!!")
    }
}

@Singleton
class ValidatorProcessor(
        private val mb: StandardMessageBusManager,
        private val hazelcastJet: HazelcastJet
) : ApplicationEventListener<ServiceStartedEvent> {

    companion object {
        private val log = LoggerFactory.getLogger(ValidatorProcessor::class.java)
    }

    override fun onApplicationEvent(event: ServiceStartedEvent?) {
        val queue = hazelcastJet.jet.hazelcastInstance.getQueue<TaskWrapper<FormSubmission>>("form-schemas-validator")

        println("setting up consumer!!")
        mb.consumer<FormSubmission>("form-submission-validation") {
            println("---->Consume start")
            println(it.messageObject::class.qualifiedName)
            println(it.messageObject.message.schema.display)
            println("---->Consume done")
        }

        Flowable.fromCallable {
            queue.take() // @TODO Add error handling
        }.doOnSubscribe {
            log.ifDebugEnabled { "Starting take()" }

        }.doOnNext {
            log.ifDebugEnabled { "Task taken from queue" }
            println("Got a Object! ${it::class.qualifiedName}")

            mb.publish("form-submission-validation") {
                MessageWrapper(it.task)
            }
        }.repeat().observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe() // @TODO add subject observer to stop it.
    }
}