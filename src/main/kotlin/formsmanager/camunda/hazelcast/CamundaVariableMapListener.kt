package formsmanager.camunda.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.listener.EntryAddedListener
import formsmanager.camunda.engine.serializer.HazelcastVariableEnricherTask
import formsmanager.camunda.engine.variable.ProcessVariable
import javax.inject.Singleton

@Singleton
class CamundaVariableMapListener(
        private val hazelcastInstance: HazelcastInstance
) : EntryAddedListener<String, ProcessVariable> {

    private val enricherExecutorService = hazelcastInstance.getDurableExecutorService("camunda-process-instance-variable-enricher")

    override fun entryAdded(event: EntryEvent<String, ProcessVariable>) {
        enricherExecutorService.submit(HazelcastVariableEnricherTask(event.value))
        //@TODO consider adding error handling here
    }
}