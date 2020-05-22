package formsmanager.camunda.management.controller

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.EntryProcessor
import com.hazelcast.query.Predicates
import formsmanager.camunda.hazelcast.ProcessVariable
import formsmanager.core.exception.AlreadyExistsException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.reactivex.Completable
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import org.camunda.bpm.engine.ProcessEngine

@Controller("/workflow/process-definition")
@RequiresGuest
class StartProcessInstanceController(
        private val engine: ProcessEngine,
        private val hazelcastInstance: HazelcastInstance
) {

    fun addVariablesMap(variables: Map<String, Any?>): Single<Map<String, String>>{
        return Single.fromCallable {
            if (variables.isNotEmpty()){
                val transaction = hazelcastInstance.newTransactionContext()
                transaction.beginTransaction()

                val map = transaction.getMap<String, ProcessVariable>("camunda-process-instance-process-variables")

                kotlin.runCatching {
                    // Remap to to internal ProcessVariable storage format
                    val newVars: List<ProcessVariable> = variables.map { (varName, varValue) ->
                        ProcessVariable(
                                engine = engine.name,
                                variableName = varName,
                                variableValue = varValue,
                                processInstanceId = null,
                                tenantId = "someTenant"
                        )
                    }

                    newVars.forEach {
                        val result: ProcessVariable? = map.putIfAbsent(it.variableId, it)

                        if (result != null){
                            throw IllegalArgumentException("Variable ID already existed...")
                        }
                    }

                    transaction.commitTransaction()

                    newVars

                }.getOrElse {
                    // @TODO Refactor this:
                    // If there was a error then we rollback transactions
                    println("ERROR with transactions for camunda variables in hazelcast")
                    it.printStackTrace()

                    transaction.rollbackTransaction()
                    throw IllegalArgumentException("Unable to complete start instance request.", it)

                }.associate {
                    Pair(it.variableName, it.variableId)
                }
            } else {
                mapOf()
            }
        }
    }

    fun removeVariables(keys: Set<String>): Completable{
        //@TODO Convert this to a distributed task.
        // Deleting does not need to occur on the same system.
        return Completable.fromAction {
            hazelcastInstance.getMap<String, ProcessVariable>("camunda-process-instance-process-variables")
                    .removeAll(Predicates.`in`("variableId", *keys.toTypedArray()))
        }
    }

    @Post("/start")
    fun startProcessInstanceByDefKey(@Body body: StartProcessInstanceRequest): Single<HttpResponse<String>> {
        return addVariablesMap(body.variables).map { camundaVariables ->
            // The data needs to already be in the map incase of sync processes that are using the variables...
            kotlin.runCatching {
                //Convert the business key usage to be a unique ID that Hazelcast can track back all activities related to the key.. ? good or bad?
                engine.runtimeService.startProcessInstanceByKey(body.key, body.businessKey, camundaVariables)
            }.onFailure {
                camundaVariables?.let {
                    // @TODO convert this to a distrobuted executiont task
                    removeVariables(it.keys)
                }
            }.getOrThrow()
            //@TODO GOES HERE --> Execute Distro Task to Update Variables with Info: Proccess Instance Id and Def Id
        }.map {
            HttpResponse.ok("processInstanceId: ${it.processInstanceId}")
        }
    }

}

class CamundaVariableEntryProcessor(private val insertValue: ProcessVariable, private val insertLogic: (insertValue: ProcessVariable) -> ProcessVariable) : EntryProcessor<String, ProcessVariable, ProcessVariable> {
    override fun process(entry: MutableMap.MutableEntry<String, ProcessVariable>): ProcessVariable {
        val value:ProcessVariable? = entry.value
        if (value != null) {
            throw AlreadyExistsException("Item ${entry.key} already exists")
        } else {
            entry.setValue(insertLogic.invoke(insertValue))
            return entry.value
        }
    }
}