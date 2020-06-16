package formsmanager.camunda.engine

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import org.camunda.bpm.engine.ProcessEngine
import java.io.File
import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@Requirements(
        Requires(beans = [ProcessEngine::class]),
        Requires(property = "camunda.bpm.autoDeploymentEnabled", value = StringUtils.TRUE, defaultValue = CamundaConfiguration.Bpm.autoDeploymentEnabled_default)
)
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
                    .tenantId("someTenant")
                    .addInputStream(it.name, it.inputStream()) // must have .bpmn in the name
                    .deployWithResult()
            println("Deployed: ${deployment.deployedProcessDefinitions}")
        }
    }
}