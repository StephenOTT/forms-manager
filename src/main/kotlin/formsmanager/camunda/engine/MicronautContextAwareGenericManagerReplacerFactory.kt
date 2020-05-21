package formsmanager.camunda.engine

import io.micronaut.context.ApplicationContext
import org.camunda.bpm.engine.impl.ProcessEngineLogger
import org.camunda.bpm.engine.impl.interceptor.Session
import org.camunda.bpm.engine.impl.interceptor.SessionFactory

/**
 * Used to replace Session Factories in Configuration with MN Injected versions.
 */
class MicronautContextAwareGenericManagerReplacerFactory(
        private val sessionTypeToReplace: Class<out Session>,
        private val managerImplementation: Class<out Session>,
        private val appCtx: ApplicationContext
) : SessionFactory {

    override fun getSessionType(): Class<*> {
        return sessionTypeToReplace
    }

    override fun openSession(): Session {
        return try {
            appCtx.inject(managerImplementation.newInstance())
        } catch (e: Exception) {
            throw LOG.instantiateSessionException(managerImplementation.name, e)
        }
    }

    companion object {
        private val LOG = ProcessEngineLogger.PERSISTENCE_LOGGER
    }
}