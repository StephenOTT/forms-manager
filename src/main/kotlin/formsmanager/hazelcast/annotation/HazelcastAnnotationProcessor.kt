package formsmanager.hazelcast.annotation

import io.micronaut.context.BeanContext
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class HazelcastAnnotationProcessor(
        private val beanContext: BeanContext
) : ExecutableMethodProcessor<HazelcastJet>, AutoCloseable {

    override fun close() {

    }

    companion object {
        private val log = LoggerFactory.getLogger(HazelcastAnnotationProcessor::class.java)
    }

    override fun process(beanDefinition: BeanDefinition<*>, method: ExecutableMethod<*, *>) {
        val qAnnotation: AnnotationValue<*>? = method.getAnnotation(QueueConsumer::class.java)

        if (qAnnotation != null) {
            val qName = qAnnotation.getRequiredValue("name", String::class.java)

            val arguments = method.arguments
            require(arguments.size == 1, lazyMessage = { "Queue methods must only have a argument of the Task type being returned" })

//            val taskType = arguments.first().type::class as KClass<ItemWrapper<*>>

            //@TODO add qualifier support
//            val instanceBean = beanContext.findBean(beanDefinition.beanType).orElseThrow { IllegalStateException("Unable to find bean instance") }
//            val eMethod = method as ExecutableMethod<Any, Unit>
//
//            log.ifDebugEnabled { "Setting up consumer!!" }
//
//            qManager.consumer(taskType, qName)
//                    .subscribeOn(Schedulers.io())
//                    .forEach {
//                        eMethod.invoke(instanceBean, it)
//                    }

        }
    }
}