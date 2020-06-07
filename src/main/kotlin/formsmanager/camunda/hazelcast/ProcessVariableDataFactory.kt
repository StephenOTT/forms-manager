package formsmanager.camunda.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.engine.variable.ProcessVariable
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class ProcessVariableDataFactory {

    @Singleton
    @Named("camunda-process-instance-process-variables")
    fun camundaVariablesMap(hazelcastInstance: HazelcastInstance): IMap<String, ProcessVariable> {
        return hazelcastInstance.getMap("camunda-process-instance-process-variables")
    }

    @Singleton
    @Named("camunda-variable-create")
    @Context
    fun camundaVariablesMapLocalProcessor(@Named("camunda-process-instance-process-variables")
                                          camundaVariablesMap: IMap<String, ProcessVariable>,
                                          listener: CamundaVariableMapListener): LocalEntryListener {
        return LocalEntryListener(camundaVariablesMap.addLocalEntryListener(listener))
    }
}