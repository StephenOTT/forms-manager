package formsmanager.core.hazelcast.context

import com.hazelcast.core.ManagedContext
import com.hazelcast.executor.impl.RunnableAdapter
import formsmanager.core.ifDebugEnabled
import io.micronaut.context.ApplicationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.reflect.full.hasAnnotation

@Singleton
class MicronautManagedContext(
        private val applicationContext: ApplicationContext
) : ManagedContext {

    companion object {
        private val log = LoggerFactory.getLogger(MicronautManagedContext::class.java)
    }

    @ExperimentalStdlibApi
    override fun initialize(instance: Any?): Any? {
        if (instance != null) {
            //@TODO ** Move this to using BeanIntrospector!!!!
            if (instance is RunnableAdapter<*>) {
                injectIfInjectAware(instance.runnable)
            } else {
                injectIfInjectAware(instance)
            }
        }

        return instance
    }

    @ExperimentalStdlibApi
    private fun injectIfInjectAware(instance: Any){
        if (instance::class.hasAnnotation<InjectAware>()){
            log.ifDebugEnabled { "Hazelcast injecting micronaut context for: ${instance::class.qualifiedName}" }
            applicationContext.inject(instance) //@TODO Review
        }
    }
}