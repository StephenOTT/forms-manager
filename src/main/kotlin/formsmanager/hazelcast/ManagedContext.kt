package formsmanager.hazelcast

import com.hazelcast.core.ManagedContext
import formsmanager.ifDebugEnabled
import io.micronaut.context.ApplicationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ManagedContext(
        private val applicationContext: ApplicationContext) : ManagedContext {

    companion object {
        private val log = LoggerFactory.getLogger(formsmanager.hazelcast.ManagedContext::class.java)
    }

    override fun initialize(instance: Any): Any {
        log.ifDebugEnabled { "Hazelcast injecting micronaut context for: ${instance::class.qualifiedName}" }

        applicationContext.inject(instance) //@TODO Review

        return instance
    }
}