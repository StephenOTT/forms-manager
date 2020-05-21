package formsmanager.camunda.engine

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import org.camunda.bpm.engine.ProcessEngine
import java.io.File
import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@Requires(beans = [ProcessEngine::class])
@Context
class AutoDeployment() {

    @PostConstruct
    fun autoDeploy(engine: ProcessEngine) {
        println("--> Starting auto deployment of BPMNs")
        val folderPath = this::class.java.getResource("/bpmn").path
        File(folderPath).walk().filter {
            it.extension == "bpmn"
        }.forEach {
            val deployment = engine.repositoryService.createDeployment()
                    .name("auto-deployment")
                    .addInputStream(it.name, it.inputStream()) // must have .bpmn in the name
                    .deployWithResult()
            println("Deployed: ${deployment.deployedProcessDefinitions}")
        }
    }
}