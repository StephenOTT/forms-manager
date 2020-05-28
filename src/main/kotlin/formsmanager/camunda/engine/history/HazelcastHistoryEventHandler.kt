package formsmanager.camunda.engine.history

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import org.camunda.bpm.engine.history.HistoricVariableInstance
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.db.HistoricEntity
import org.camunda.bpm.engine.impl.history.event.*
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
import javax.inject.Singleton

@Singleton
class HazelcastHistoryEventHandler(
        private val hazelcastInstance: HazelcastInstance
) : HistoryEventHandler {

    private val historyMaps: Map<Class<out HistoricEntity>, IMap<String, out HistoricEntity>> = mapOf(
            Pair(HistoricActivityInstanceEventEntity::class.java, hazelcastInstance.getMap<String, HistoricActivityInstanceEventEntity>("camunda-history-activity-instance")),
            Pair(HistoricCaseActivityInstanceEventEntity::class.java, hazelcastInstance.getMap<String, HistoricCaseActivityInstanceEventEntity>("camunda-history-case-activity-instance")),
            Pair(HistoricCaseInstanceEventEntity::class.java, hazelcastInstance.getMap<String, HistoricCaseInstanceEventEntity>("camunda-history-case-instance")),
            Pair(HistoricDecisionEvaluationEvent::class.java, hazelcastInstance.getMap<String, HistoricDecisionEvaluationEvent>("camunda-history-decision-evaluation")),
//            Pair(HistoricDecisionInputInstanceEntity::class.java, hazelcastInstance.getMap<String, HistoricDecisionInputInstanceEntity>("camunda-history-decision-input-instance")),
//            Pair(HistoricDecisionOutputInstanceEntity::class.java, hazelcastInstance.getMap<String, HistoricDecisionOutputInstanceEntity>("camunda-history-decision-output-instance")),
            Pair(HistoricDecisionInstanceEntity::class.java, hazelcastInstance.getMap<String, HistoricDecisionInstanceEntity>("camunda-history-decision-instance")),
            Pair(HistoricDetailEventEntity::class.java, hazelcastInstance.getMap<String, HistoricDetailEventEntity>("camunda-history-historic-detail")),
            Pair(HistoricExternalTaskLogEntity::class.java, hazelcastInstance.getMap<String, HistoricExternalTaskLogEntity>("camunda-history-external-task-log")),
            Pair(HistoricFormPropertyEventEntity::class.java, hazelcastInstance.getMap<String, HistoricFormPropertyEventEntity>("camunda-history-form-property")),
            Pair(HistoricIdentityLinkLogEventEntity::class.java, hazelcastInstance.getMap<String, HistoricIdentityLinkLogEventEntity>("camunda-history-identity-link-log")),
            Pair(HistoricIncidentEventEntity::class.java, hazelcastInstance.getMap<String, HistoricIncidentEventEntity>("camunda-history-incident")),
            Pair(HistoricJobLogEvent::class.java, hazelcastInstance.getMap<String, HistoricJobLogEvent>("camunda-history-job-log")),
            Pair(HistoricProcessInstanceEventEntity::class.java, hazelcastInstance.getMap<String, HistoricProcessInstanceEventEntity>("camunda-history-process-instance")),
            Pair(HistoricTaskInstanceEventEntity::class.java, hazelcastInstance.getMap<String, HistoricTaskInstanceEventEntity>("camunda-history-task-instance")),
            Pair(UserOperationLogEntryEventEntity::class.java, hazelcastInstance.getMap<String, UserOperationLogEntryEventEntity>("camunda-history-user-operation-log")),
            Pair(HistoricVariableInstanceEntity::class.java, hazelcastInstance.getMap<String, HistoricVariableInstanceEntity>("camunda-history-variable-instance"))


    )

    private fun <T : HistoricEntity> getHistoryMap(clazz: Class<out T>): IMap<String, T> {
        return historyMaps.filterKeys { it == clazz }.entries.single().value as IMap<String, T>
    }


    override fun handleEvent(historyEvent: HistoryEvent) {
        when (historyEvent) {
            is HistoricVariableUpdateEventEntity -> {
                insertHistoricVariableUpdateEntity(historyEvent)
            }
            is HistoricDecisionEvaluationEvent -> {
                insertHistoricDecisionEvaluationEvent(historyEvent)
            }
            else -> {
                insertOrUpdate(historyEvent)
            }
        }
    }

    private fun insertHistoricDecisionEvaluationEvent(event: HistoricDecisionEvaluationEvent) {
        if (true) { //change to check if History is enabled
            val rootHistoricDecisionInstance = event.rootHistoricDecisionInstance
            insertHistoricDecisionInstance(rootHistoricDecisionInstance)
            for (requiredHistoricDecisionInstances in event.requiredHistoricDecisionInstances) {
                requiredHistoricDecisionInstances.rootDecisionInstanceId = rootHistoricDecisionInstance.id
                insertHistoricDecisionInstance(requiredHistoricDecisionInstances)
            }
        }
    }

    private fun insertHistoricDecisionInstance(historicDecisionInstance: HistoricDecisionInstanceEntity) {
        val map = getHistoryMap(historicDecisionInstance::class.java)
        map[historicDecisionInstance.id] = historicDecisionInstance
        // Dont need the nested objects because we can save it as a single object and query on it later.!! :)


//        insertHistoricDecisionInputInstances(historicDecisionInstance.inputs, historicDecisionInstance.id)
//        insertHistoricDecisionOutputInstances(historicDecisionInstance.outputs, historicDecisionInstance.id)
    }

//    protected fun insertHistoricDecisionInputInstances(inputs: List<HistoricDecisionInputInstance>, decisionInstanceId: String?) {
//        for (input in inputs) {
//            val inputEntity = input as HistoricDecisionInputInstanceEntity
//            inputEntity.decisionInstanceId = decisionInstanceId
//            getDbEntityManager().insert(inputEntity)
//        }
//    }
//
//    protected fun insertHistoricDecisionOutputInstances(outputs: List<HistoricDecisionOutputInstance>, decisionInstanceId: String?) {
//        for (output in outputs) {
//            val outputEntity = output as HistoricDecisionOutputInstanceEntity
//            outputEntity.decisionInstanceId = decisionInstanceId
//            getDbEntityManager().insert(outputEntity)
//        }
//    }


    private fun shouldWriteHistoricDetail(historyEvent: HistoricVariableUpdateEventEntity): Boolean {
        // @TODO convert this to injection usage instead of Context
        return (Context.getProcessEngineConfiguration().historyLevel
                .isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE_DETAIL, historyEvent)
                && !historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE))
    }

    protected fun insertHistoricVariableUpdateEntity(historyEvent: HistoricVariableUpdateEventEntity) {

        // insert update only if history level = FULL
        if (shouldWriteHistoricDetail(historyEvent)) {
            val map = getHistoryMap(historyEvent::class.java)
            // If its a Historic Detail, then just add it
            map[historyEvent.id] = historyEvent
        }

        // always insert/update HistoricProcessVariableInstance
        if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_CREATE)) {
            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)
            val persistentObject = HistoricVariableInstanceEntity(historyEvent)
            map[persistentObject.id] = persistentObject



        } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE)
                || historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE)) {

            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)
            val historicVariableInstanceEntity = map[historyEvent.variableInstanceId]
            checkNotNull(historicVariableInstanceEntity) {
                "Unable to find historic variable instance with ID: ${historyEvent.variableInstanceId}.  ${historyEvent}"
            }

            val persistentObject = HistoricVariableInstanceEntity(historyEvent)
            persistentObject.state = HistoricVariableInstance.STATE_CREATED

            map.replace(persistentObject.id, persistentObject)

        } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_DELETE)) {
            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)
            val historicVariableInstanceEntity = map[historyEvent.variableInstanceId]
            checkNotNull(historicVariableInstanceEntity) {
                "Unable to find historic variable instance with ID: ${historyEvent.variableInstanceId}.  ${historyEvent}"
            }

            historicVariableInstanceEntity.state = HistoricVariableInstance.STATE_DELETED

            map.replace(historicVariableInstanceEntity.id, historicVariableInstanceEntity)
        }
    }


    override fun handleEvents(historyEvents: List<HistoryEvent>) {
        historyEvents.forEach {
            insertOrUpdate(it)
        }
    }


    private fun insertOrUpdate(historyEvent: HistoryEvent) {
        val map = getHistoryMap(historyEvent::class.java)

        if (isInitialEvent(historyEvent)) {
            map[historyEvent.id] = historyEvent

        } else {
            val existingEvent = map[historyEvent.id]

            checkNotNull(existingEvent) {
                "Unable to find expected existing history event for ${historyEvent}"
            }

            if (historyEvent is HistoricScopeInstanceEvent) {
                historyEvent.startTime = (existingEvent as HistoricScopeInstanceEvent).startTime
            }

            map.replace(historyEvent.id, existingEvent, historyEvent)
        }
    }

    private fun isInitialEvent(historyEvent: HistoryEvent): Boolean {
        return (historyEvent.eventType == null
                || historyEvent.isEventOfType(HistoryEventTypes.ACTIVITY_INSTANCE_START)
                || historyEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_START)
                || historyEvent.isEventOfType(HistoryEventTypes.TASK_INSTANCE_CREATE)
                || historyEvent.isEventOfType(HistoryEventTypes.FORM_PROPERTY_UPDATE)
                || historyEvent.isEventOfType(HistoryEventTypes.INCIDENT_CREATE)
                || historyEvent.isEventOfType(HistoryEventTypes.CASE_INSTANCE_CREATE)
                || historyEvent.isEventOfType(HistoryEventTypes.DMN_DECISION_EVALUATE)
                || historyEvent.isEventOfType(HistoryEventTypes.BATCH_START)
                || historyEvent.isEventOfType(HistoryEventTypes.IDENTITY_LINK_ADD)
                || historyEvent.isEventOfType(HistoryEventTypes.IDENTITY_LINK_DELETE))
    }
}