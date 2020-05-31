package formsmanager.camunda.engine.serializer

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer
import javax.inject.Named
import javax.inject.Singleton

@Factory
class VariableSerializerFactory {

    @Singleton
    @Named("custom-variable-serializers")
    fun externalTaskService(variableSerializers: List<AbstractTypedValueSerializer<*>>): MicronautProcessEnginePlugin {
        return object : MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customPostVariableSerializers == null) {
                    processEngineConfiguration.customPostVariableSerializers = mutableListOf()
                }
                processEngineConfiguration.customPostVariableSerializers.addAll(variableSerializers)
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }
    }
}