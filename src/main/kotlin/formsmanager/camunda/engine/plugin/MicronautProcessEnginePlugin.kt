package formsmanager.camunda.engine.plugin

import io.micronaut.core.order.Ordered
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin

/**
 * Wrapper for ProcessEnginePlugin.  Provides the Ordered interface for controlling the order of Plugin execution
 * Beans using this interface will be automatically added into engine configuration at startup.
 */
interface MicronautProcessEnginePlugin: ProcessEnginePlugin, Ordered {
    companion object {
        const val DEFAULT_ORDER: Int = 0
    }
}