package formsmanager.camunda.engine.serializer

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hazelcast.map.IMap
import formsmanager.camunda.engine.variable.ProcessVariable
import formsmanager.core.hazelcast.context.InjectAware
import formsmanager.core.hazelcast.task.TaskWithoutReturn
import formsmanager.core.ifDebugEnabled
import io.micronaut.context.annotation.Parameter
import org.camunda.bpm.engine.ProcessEngine
import javax.inject.Inject
import javax.inject.Named

@InjectAware
class HazelcastVariableEnricherTask(
        @Parameter val processVariable: ProcessVariable
) : TaskWithoutReturn() {

    @Transient @Inject @JsonIgnore
    private lateinit var processEngine: ProcessEngine

    @Transient @Inject @JsonIgnore
    @Named("camunda-process-instance-process-variables")
    private lateinit var camundaVariablesMap: IMap<String, ProcessVariable>

    override fun run() {
        val camVariable = processEngine.historyService.createNativeHistoricVariableInstanceQuery()
                .sql("SELECT * FROM act_hi_varinst WHERE NAME_ = #{name} AND TEXT_ = #{value}")
                .parameter("name", processVariable.variableName)
                .parameter("value", processVariable.mapKey)
                .disableCustomObjectDeserialization()
                .singleResult()

        checkNotNull(camVariable){
            "Unable to find Camunda variable for name: ${processVariable.variableName} with mapKey: ${processVariable.mapKey}"
        }

        val hydrated = processVariable.copy(
                processDefinitionKey = camVariable.processDefinitionKey,
                processDefinitionId = camVariable.processDefinitionId,
                processInstanceId = camVariable.processInstanceId,
                tenantId = camVariable.tenantId,
                activityInstanceId = if (camVariable.activityInstanceId == camVariable.processInstanceId) null else camVariable.activityInstanceId,
                scope = if (camVariable.activityInstanceId == camVariable.processInstanceId) "process" else "local",
                createdAt = camVariable.createTime.toInstant()
        )

        //@TODO consider a entryProcessor instead:
        val replacer = camundaVariablesMap.replace(hydrated.mapKey, processVariable, hydrated)
        if (!replacer) {
            LOG.error("Unable to enrich Camunda Variable: ${processVariable.mapKey}")
        } else {
            LOG.ifDebugEnabled { "Successfully Enriched Process Variable ${hydrated.variableName} in MapKey ${hydrated.mapKey}" }
        }
    }
}