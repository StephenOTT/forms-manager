package formsmanager.hazelcast

import com.hazelcast.collection.IQueue
import formsmanager.domain.FormSchema
import formsmanager.ifDebugEnabled
import formsmanager.validator.FormSubmission
import formsmanager.validator.FormSubmissionData
import formsmanager.validator.queue.TaskWrapper
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.DefaultScope
import io.micronaut.context.annotation.Executable
import io.micronaut.context.annotation.Parallel
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.scheduling.executor.ExecutorConfiguration
import io.micronaut.scheduling.executor.ExecutorFactory
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.*
import javax.inject.Singleton
import kotlin.reflect.KClass

@HazelcastConsumer
class MyConsumer {

    @Queue("form-schemas-validator")
    fun myQueue1(task: TaskWrapper<FormSubmission>) {
        println("dogs!!!!")
    }
}


@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Bean
@DefaultScope(Singleton::class)
@Executable(processOnStartup = true)
@Parallel
annotation class HazelcastConsumer {}

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Queue(
        val name: String
) {}

@Singleton
class HazelcastConsumerAdvice(
        private val beanContext: BeanContext,
        private val qManager: QueueManager
) : ExecutableMethodProcessor<HazelcastConsumer>, AutoCloseable {

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun process(beanDefinition: BeanDefinition<*>, method: ExecutableMethod<*, *>) {
        val qAnnotation: AnnotationValue<*>? = method.getAnnotation(Queue::class.java)

        if (qAnnotation != null) {
            val qName = qAnnotation.getRequiredValue("name", String::class.java)

            val arguments = method.arguments
            require(arguments.size == 1, lazyMessage = { "Queue methods must only have a argument of the Task type being returned" })

            val taskType = arguments.first().type::class

            //@TODO add qualifier support
            val instanceBean = beanContext.findBean(beanDefinition.beanType).orElseThrow { IllegalStateException("Unable to find bean instance") }

            qManager.createQueueConsumer(qName, taskType, method, instanceBean)
            println("startup done")
        }
    }
}


@Singleton
class QueueManager(
        private val hazelcastJet: HazelcastJet,
        private val mb: StandardMessageBusManager
) {

    companion object{
        private val log = LoggerFactory.getLogger(QueueManager::class.java)
    }

    fun <T : Any> createQueueConsumer(qName: String, taskType: KClass<T>, method: ExecutableMethod<*, *>, instanceBean: Any) {
        log.ifDebugEnabled { "Starting Hazelcast Queue Consumer for $qName for ${taskType.qualifiedName}" }
        val queue = hazelcastJet.jet.hazelcastInstance.getQueue<T>(qName)

        queue.put(TaskWrapper<FormSubmission>(
                UUID.randomUUID(), null, "sample",
                FormSubmission(FormSchema("12", listOf(mapOf())),
                        FormSubmissionData(mapOf(), null))
        ) as T)

        queue.put(TaskWrapper<FormSubmission>(
                UUID.randomUUID(), null, "sample",
                FormSubmission(FormSchema("12", listOf(mapOf())),
                        FormSubmissionData(mapOf(), null))
        ) as T)

        QueueWorker(queue,qName,taskType,mb)
    }
}

class QueueWorker(
        val queue: IQueue<*>,
        val qName: String,
        val taskType: KClass<*>,
        val mb: StandardMessageBusManager
){
    init {
        start().observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe() // @TODO add subject observer to stop it.
    }

    companion object{
        private val log = LoggerFactory.getLogger(QueueWorker::class.java)
    }

    private fun start(): Observable<*>{
        return Observable.fromCallable {

        }
//        return Observable.fromCallable {
//            queue.take()
//        }.doOnSubscribe {
//            log.ifDebugEnabled { "Starting take() for $qName for ${taskType.qualifiedName}" }
//        }.doOnNext {
//            log.ifDebugEnabled { "Task taken from queue $qName for ${taskType.qualifiedName}: $it" }
//            println("GOt a Object!")
//            println(it::class.qualifiedName)
//
//            mb.publish("form-submission-validation"){
//                MessageWrapper(message = (it as TaskWrapper<FormSubmission>))
//            }
//        }.repeat()
    }
}