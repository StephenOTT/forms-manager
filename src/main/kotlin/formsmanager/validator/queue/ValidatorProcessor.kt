package formsmanager.validator.queue

import formsmanager.hazelcast.*
import formsmanager.ifDebugEnabled
import formsmanager.validator.FormSubmission
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.discovery.event.ServiceStartedEvent
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ValidatorProcessor(
        private val mb: StandardMessageBusManager,
        private val hazelcastJet: HazelcastJet
) : ApplicationEventListener<ServiceStartedEvent> {

    companion object{
        private val log = LoggerFactory.getLogger(ValidatorProcessor::class.java)
    }

    override fun onApplicationEvent(event: ServiceStartedEvent?) {
        val queue = hazelcastJet.jet.hazelcastInstance.getQueue<TaskWrapper<FormSubmission>>("form-schemas-validator")

        println("setting up consumer!!")
        mb.consumer<FormSubmission>("form-submission-validation") {
            println("---->start")
            println(it.messageObject::class.qualifiedName)
            println(it.messageObject.message.schema.display)
            println("---->done")
        }

        Observable.fromCallable {
            queue.take()
        }.doOnSubscribe {
            log.ifDebugEnabled { "Starting take()" }
        }.doOnNext {
            log.ifDebugEnabled { "Task taken from queue" }
            println("GOt a Object!")
            println(it::class.qualifiedName)

            mb.publish("form-submission-validation"){
                MessageWrapper(it.task)
            }
        }.repeat().observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe() // @TODO add subject observer to stop it.
    }
}