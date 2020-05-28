package formsmanager.camunda.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import com.hazelcast.map.listener.EntryAddedListener
import formsmanager.camunda.engine.serializer.HazelcastVariableEnricherTask
import formsmanager.camunda.engine.variable.ProcessVariable
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Factory
class CommonDataFactory {

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

@Singleton
class CamundaVariableMapListener(
        private val hazelcastInstance: HazelcastInstance
) : EntryAddedListener<String, ProcessVariable> {

    private val LOG = LoggerFactory.getLogger(CamundaVariableMapListener::class.java)

    override fun entryAdded(event: EntryEvent<String, ProcessVariable>) {
        val enricherExecutorService = hazelcastInstance.getDurableExecutorService("camunda-process-instance-variable-enricher")
        enricherExecutorService.submit(HazelcastVariableEnricherTask(event.value))
        //@TODO consider adding error handling here
    }
}

data class LocalEntryListener(val value: UUID)