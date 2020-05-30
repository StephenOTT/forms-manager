package formsmanager.camunda.engine.parselistener

import formsmanager.camunda.engine.plugin.MicronautProcessEnginePlugin
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import javax.inject.Named
import javax.inject.Singleton

@Factory
class ParseListenerFactory {

    @Singleton
    @Named("parse-listeners")
    fun parseListenerPlugin(parseListeners: List<MicronautBpmnParseListener>): MicronautProcessEnginePlugin {
        return object : MicronautProcessEnginePlugin {

            override fun getOrder(): Int {
                return MicronautProcessEnginePlugin.DEFAULT_ORDER + 1
            }

            override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
                if (processEngineConfiguration.customPreBPMNParseListeners == null) {
                    processEngineConfiguration.customPreBPMNParseListeners = mutableListOf()
                }
                if (processEngineConfiguration.customPostBPMNParseListeners == null) {
                    processEngineConfiguration.customPostBPMNParseListeners = mutableListOf()
                }

                parseListeners.forEach {
                    when (it) {
                        is PreParse -> {
                            processEngineConfiguration.customPreBPMNParseListeners.add(it)
                        }
                        is PostParse -> {
                            processEngineConfiguration.customPostBPMNParseListeners.add(it)
                        }
                        else -> {
                            // Default is to add as a PreParse:
                            processEngineConfiguration.customPreBPMNParseListeners.add(it)
                        }
                    }
                }
            }

            override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            }

            override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            }
        }

    }
}