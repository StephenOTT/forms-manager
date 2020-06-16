package formsmanager.camunda.hazelcast.history

import com.hazelcast.core.HazelcastInstance
import formsmanager.camunda.hazelcast.history.repository.*
import org.camunda.bpm.engine.history.HistoricVariableInstance
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.db.HistoricEntity
import org.camunda.bpm.engine.impl.history.event.*
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
import javax.inject.Singleton

@Singleton
class HazelcastHistoryEventHandler(
        private val hazelcastInstance: HazelcastInstance,
        private val historicActivityInstanceHazelcastRepository: HistoricActivityInstanceHazelcastRepository,
        private val historicCaseActivityInstanceHazelcastRepository: HistoricActivityInstanceHazelcastRepository,
        private val historicCaseInstanceHazelcastRepository: HistoricCaseInstanceHazelcastRepository,
        private val historicDetailHazelcastRepository: HistoricDetailHazelcastRepository,
        private val historicExternalTaskLogHazelcastRepository: HistoricExternalTaskLogHazelcastRepository,
        private val historicFormPropertyHazelcastRepository: HistoricFormPropertyHazelcastRepository,
        private val historicIncidentHazelcastRepository: HistoricIncidentHazelcastRepository,
        private val historicIdentityLinkLogHazelcastRepository: HistoricIdentityLinkLogHazelcastRepository,
        private val historicJobLogHazelcastRepository: HistoricJobLogHazelcastRepository,
        private val historicProcessInstanceHazelcastRepository: HistoricProcessInstanceHazelcastRepository,
        private val historicTaskInstanceHazelcastRepository: HistoricTaskInstanceHazelcastRepository,
        private val historicUserOperationLogHazelcastRepository: HistoricUserOperationLogHazelcastRepository,
        private val historicVariableInstanceHazelcastRepository: HistoricVariableInstanceHazelcastRepository,
        private val historicDecisionInstanceHazelcastRepository: HistoricDecisionInstanceHazelcastRepository
) : HistoryEventHandler {

    private val historyMaps: Map<Class<out HistoricEntity>, CamundaHistoricEventReactiveRepository<*, *>> = mapOf(
            Pair(HistoricActivityInstanceEventEntity::class.java, historicActivityInstanceHazelcastRepository),
            Pair(HistoricCaseActivityInstanceEventEntity::class.java, historicCaseActivityInstanceHazelcastRepository),
            Pair(HistoricCaseInstanceEventEntity::class.java, historicCaseInstanceHazelcastRepository),
            //@TODO re-eval the DecisionEval History and make Repos for them!!
//            Pair(HistoricDecisionEvaluationEvent::class.java, ---),
//            Pair(HistoricDecisionInputInstanceEntity::class.java, ---),
//            Pair(HistoricDecisionOutputInstanceEntity::class.java, ---),
            Pair(HistoricDecisionInstanceEntity::class.java, historicDecisionInstanceHazelcastRepository),
            Pair(HistoricDetailEventEntity::class.java, historicDetailHazelcastRepository),
            Pair(HistoricExternalTaskLogEntity::class.java, historicExternalTaskLogHazelcastRepository),
            Pair(HistoricFormPropertyEventEntity::class.java, historicFormPropertyHazelcastRepository),
            Pair(HistoricIdentityLinkLogEventEntity::class.java, historicIdentityLinkLogHazelcastRepository),
            Pair(HistoricIncidentEventEntity::class.java, historicIncidentHazelcastRepository),
            Pair(HistoricJobLogEvent::class.java, historicJobLogHazelcastRepository),
            Pair(HistoricProcessInstanceEventEntity::class.java, historicProcessInstanceHazelcastRepository),
            Pair(HistoricTaskInstanceEventEntity::class.java, historicTaskInstanceHazelcastRepository),
            Pair(UserOperationLogEntryEventEntity::class.java, historicUserOperationLogHazelcastRepository),
            Pair(HistoricVariableInstanceEntity::class.java, historicVariableInstanceHazelcastRepository)


    )

    private fun <T : HistoricEntity> getHistoryMap(clazz: Class<out T>): CamundaHistoricEventReactiveRepository<String, T> {
        return historyMaps.filterKeys { it == clazz }.entries.single().value as CamundaHistoricEventReactiveRepository<String, T>
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
        // @TODO the `true` change to check if History is enabled
        if (true) {
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

        map.create(historicDecisionInstance.id, historicDecisionInstance).blockingGet()
        // Dont need the nested objects because we can save it as a single object and query on it later.!! :)


//        insertHistoricDecisionInputInstances(historicDecisionInstance.inputs, historicDecisionInstance.id)
//        insertHistoricDecisionOutputInstances(historicDecisionInstance.outputs, historicDecisionInstance.id)
    }

    // Not needed:
//    protected fun insertHistoricDecisionInputInstances(inputs: List<HistoricDecisionInputInstance>, decisionInstanceId: String?) {
//        for (input in inputs) {
//            val inputEntity = input as HistoricDecisionInputInstanceEntity
//            inputEntity.decisionInstanceId = decisionInstanceId
//            getDbEntityManager().insert(inputEntity)
//        }
//    }
//
    // not needed
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
            map.create(historyEvent.id, historyEvent).blockingGet()
        }

        // always insert/update HistoricProcessVariableInstance
        if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_CREATE)) {
            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)
            val persistentObject = HistoricVariableInstanceEntity(historyEvent)

            map.create(persistentObject.id, persistentObject).blockingGet()
//            map[persistentObject.id] = persistentObject


        } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE)
                || historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE)) {

            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)
//            val historicVariableInstanceEntity = map[historyEvent.variableInstanceId]
            val historicVariableInstanceEntity = map.get(historyEvent.variableInstanceId).blockingGet()
            checkNotNull(historicVariableInstanceEntity) {
                "Unable to find historic variable instance with ID: ${historyEvent.variableInstanceId}.  ${historyEvent}"
            }

            val persistentObject = HistoricVariableInstanceEntity(historyEvent)
            persistentObject.state = HistoricVariableInstance.STATE_CREATED

//            map.replace(persistentObject.id, persistentObject)
            map.update(persistentObject.id, persistentObject).blockingGet()

        } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_DELETE)) {
            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)
//
//            val historicVariableInstanceEntity = map[historyEvent.variableInstanceId]
            val historicVariableInstanceEntity = map.get(historyEvent.variableInstanceId).blockingGet()

            checkNotNull(historicVariableInstanceEntity) {
                "Unable to find historic variable instance with ID: ${historyEvent.variableInstanceId}.  ${historyEvent}"
            }

            historicVariableInstanceEntity.state = HistoricVariableInstance.STATE_DELETED

//            map.replace(historicVariableInstanceEntity.id, historicVariableInstanceEntity)
            map.update(historicVariableInstanceEntity.id, historicVariableInstanceEntity).blockingGet()
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
//            map[historyEvent.id] = historyEvent
            map.create(historyEvent.id, historyEvent).blockingGet()

        } else {
//            val existingEvent = map[historyEvent.id]
            val existingEvent = map.get(historyEvent.id).blockingGet()

            checkNotNull(existingEvent) {
                "Unable to find expected existing history event for ${historyEvent}"
            }

            if (historyEvent is HistoricScopeInstanceEvent) {
                historyEvent.startTime = (existingEvent as HistoricScopeInstanceEvent).startTime
            }

            //@TODO Review!!:
//            map.replace(historyEvent.id, existingEvent, historyEvent)
            map.update(historyEvent.id, historyEvent).blockingGet()
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