package formsmanager.hazelcast.annotation

import formsmanager.domain.FormSchema
import formsmanager.hazelcast.HazelcastJet
import formsmanager.hazelcast.topic.StandardMessageBusManager
import formsmanager.ifDebugEnabled
import formsmanager.validator.FormSubmission
import formsmanager.validator.FormSubmissionData
import formsmanager.hazelcast.queue.QueueWorker
import formsmanager.validator.queue.TaskWrapper
import io.micronaut.context.BeanContext
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class HazelcastAnnotationProcessor(
        private val beanContext: BeanContext,
        private val qManager: QueueManager
) : ExecutableMethodProcessor<Hazelcast>, AutoCloseable {

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun process(beanDefinition: BeanDefinition<*>, method: ExecutableMethod<*, *>) {
        val qAnnotation: AnnotationValue<*>? = method.getAnnotation(QueueConsumer::class.java)

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

        QueueWorker(queue, qName, taskType, mb)
    }
}