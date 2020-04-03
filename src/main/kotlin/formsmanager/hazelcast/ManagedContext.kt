package formsmanager.hazelcast

import com.hazelcast.core.ManagedContext
import io.micronaut.context.ApplicationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ManagedContext (
        private val applicationContext: ApplicationContext) : ManagedContext {

    companion object{
        private val LOG = LoggerFactory.getLogger(formsmanager.hazelcast.ManagedContext::class.java)
    }

    override fun initialize(instance: Any): Any {
        if (LOG.isDebugEnabled){
            LOG.debug("Hazelcast injecting micronaut context for: ${instance::class.qualifiedName}")
        }
        applicationContext.inject(instance)
        return instance
    }
}