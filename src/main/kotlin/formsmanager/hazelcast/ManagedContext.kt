package formsmanager.hazelcast

import com.hazelcast.core.ManagedContext
import formsmanager.ifDebugEnabled
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.reflect.full.hasAnnotation

@Singleton
class ManagedContext(
        private val applicationContext: ApplicationContext,
        private val beanContext: BeanContext
        ) : ManagedContext {

    companion object {
        private val log = LoggerFactory.getLogger(formsmanager.hazelcast.ManagedContext::class.java)
    }

    @ExperimentalStdlibApi
    override fun initialize(instance: Any): Any {

        if (instance::class.hasAnnotation<InjectAware>()){
            log.ifDebugEnabled { "Hazelcast injecting micronaut context for: ${instance::class.qualifiedName}" }
            applicationContext.inject(instance) //@TODO Review
        }

        return instance
    }
}

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectAware {

}