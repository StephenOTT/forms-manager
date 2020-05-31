package formsmanager.camunda.engine.expression

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.el.ExpressionManager
import javax.inject.Singleton

@Singleton
@Requires(beans = [ExpressionManager::class])
class ExpressionManagerPlugin(
        private val expressionManager: ExpressionManager
) : MicronautProcessEnginePlugin{
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        processEngineConfiguration.expressionManager = expressionManager
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }
}