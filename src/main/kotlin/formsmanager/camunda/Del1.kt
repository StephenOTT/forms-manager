package formsmanager.camunda

import formsmanager.camunda.hazelcast.variable.HazelcastVariable
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.db.HistoricEntity
import java.util.*
import javax.inject.Named

@Named("create1")
class Del1 : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        execution.setVariable("dog", HazelcastVariable("dog","frank"))
        execution.setVariable("n1", "v1")
        execution.setVariable("aMap", mapOf(Pair("a","v1"),Pair("b","v2")))
        execution.setVariable("nullValue", null)
    }
}

@Named("update1")
class Del2 : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        execution.setVariable("dog", HazelcastVariable("dog","steve"))
        execution.setVariable("n1", "v2")
    }
}


data class OptimizedHistoricVariableInstanceEntity(
    val id: String,
    val processDefinitionKey: String? = null,
    val processDefinitionId: String? = null,
    val rootProcessInstanceId: String? = null,
    val processInstanceId: String? = null,
    val taskId: String? = null,
    val executionId: String? = null,
    val activityInstanceId: String? = null,
    val tenantId: String? = null,
    val caseDefinitionKey: String? = null,
    val caseDefinitionId: String? = null,
    val caseInstanceId: String? = null,
    val caseExecutionId: String? = null,
    val name: String? = null,
    val revision: Int = 0,
    val createTime: Date? = null,
    val longValue: Long? = null,
    val doubleValue: Double? = null,
    val textValue: String? = null,
    val textValue2: String? = null,
    val state: String = "CREATED",
    val removalTime: Date? = null,
    val typedValueClass: String? = null,
    val typedValue: Any? = null
): HistoricEntity {
    init {
        if (typedValue != null){
            require(typedValueClass != null){
                "typedValueClass cannot be null if typedValue is not null."
            }
        }
    }
}