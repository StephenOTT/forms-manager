package formsmanager.camunda.engine.plugin

import io.micronaut.core.order.Ordered
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin

interface MicronautProcessEnginePlugin: ProcessEnginePlugin, Ordered {
    companion object {
        const val DEFAULT_ORDER: Int = 0
    }

}