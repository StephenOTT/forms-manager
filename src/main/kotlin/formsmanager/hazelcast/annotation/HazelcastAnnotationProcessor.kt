package formsmanager.hazelcast.annotation

import formsmanager.hazelcast.HazelcastTransportable
import formsmanager.hazelcast.queue.QueueManager
import io.micronaut.context.BeanContext
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class HazelcastAnnotationProcessor(
        private val beanContext: BeanContext,
        private val qManager: QueueManager
) : ExecutableMethodProcessor<formsmanager.hazelcast.annotation.HazelcastJet>, AutoCloseable {

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun process(beanDefinition: BeanDefinition<*>, method: ExecutableMethod<*, *>) {
        val qAnnotation: AnnotationValue<*>? = method.getAnnotation(QueueConsumer::class.java)

        if (qAnnotation != null) {
            val qName = qAnnotation.getRequiredValue("name", String::class.java)

            val arguments = method.arguments
            require(arguments.size == 1, lazyMessage = { "Queue methods must only have a argument of the Task type being returned" })

            val taskType = arguments.first().type::class as KClass<HazelcastTransportable>

            //@TODO add qualifier support
            val instanceBean = beanContext.findBean(beanDefinition.beanType).orElseThrow { IllegalStateException("Unable to find bean instance") }
            val eMethod = method as ExecutableMethod<Any, Unit>

            qManager.consumer(taskType, qName){
                eMethod.invoke(instanceBean, it)
            }
            println("startup done")
        }
    }
}