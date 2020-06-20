package formsmanager.camunda.hazelcast.history

import formsmanager.camunda.OptimizedHistoricVariableInstanceEntity
import formsmanager.camunda.hazelcast.history.repository.*
import formsmanager.core.ifDebugEnabled
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.history.HistoricVariableInstance
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.db.HistoricEntity
import org.camunda.bpm.engine.impl.history.event.*
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class HazelcastHistoryEventHandler(
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
        private val historicDecisionInstanceHazelcastRepository: HistoricDecisionInstanceHazelcastRepository,
        private val optimizedHistoricVariableInstanceHazelcastRepository: OptimizedHistoricVariableInstanceHazelcastRepository,
        private val processEngine: ProcessEngine
) : HistoryEventHandler {

    private val log = LoggerFactory.getLogger(HazelcastHistoryEventHandler::class.java)

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
            Pair(HistoricJobLogEventEntity::class.java, historicJobLogHazelcastRepository),
            Pair(HistoricProcessInstanceEventEntity::class.java, historicProcessInstanceHazelcastRepository),
            Pair(HistoricTaskInstanceEventEntity::class.java, historicTaskInstanceHazelcastRepository),
            Pair(UserOperationLogEntryEventEntity::class.java, historicUserOperationLogHazelcastRepository),
//            Pair(HistoricVariableInstanceEntity::class.java, historicVariableInstanceHazelcastRepository),
            Pair(OptimizedHistoricVariableInstanceEntity::class.java, optimizedHistoricVariableInstanceHazelcastRepository)


    )

    /**
     * Get the repository for the specific HistoricEntity
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : HistoricEntity> getHistoryMap(clazz: Class<out T>): CamundaHistoricEventReactiveRepository<String, T> {
        log.ifDebugEnabled { "Looking for HistoryMap class: ${clazz.canonicalName}" }
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
    // Not needed:
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
            val map = getHistoryMap(HistoricDetailEventEntity::class.java)
                if (historyEvent.id == null){
                    historyEvent.id = (processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl).idGenerator.nextId
                }
            // If its a Historic Detail, then just add it
            map.create(historyEvent.id, historyEvent).blockingGet()
        }

        // always insert/update HistoricProcessVariableInstance
        if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_CREATE)) {

            val map = getHistoryMap(OptimizedHistoricVariableInstanceEntity::class.java)
            val persistentObject0 = HistoricVariableInstanceEntity(historyEvent)

            val value: Any? = persistentObject0.getTypedValue(true).value
            val persistentObject = OptimizedHistoricVariableInstanceEntity(
                    state = persistentObject0.state,
                    id = persistentObject0.id,
                    processDefinitionKey = persistentObject0.processDefinitionKey,
                    processDefinitionId = persistentObject0.processDefinitionId,
                    rootProcessInstanceId = persistentObject0.rootProcessInstanceId,
                    processInstanceId = persistentObject0.processInstanceId,
                    taskId = persistentObject0.taskId,
                    executionId = persistentObject0.executionId,
                    activityInstanceId = persistentObject0.activityInstanceId,
                    tenantId = persistentObject0.tenantId,
                    caseDefinitionKey = persistentObject0.caseDefinitionKey,
                    caseDefinitionId = persistentObject0.caseDefinitionId,
                    caseInstanceId = persistentObject0.caseInstanceId,
                    caseExecutionId = persistentObject0.caseExecutionId,
                    name = persistentObject0.variableName,
                    revision = persistentObject0.revision,
                    createTime = persistentObject0.createTime,
                    longValue = persistentObject0.longValue,
                    doubleValue = persistentObject0.doubleValue,
                    textValue = persistentObject0.textValue,
                    textValue2 = persistentObject0.textValue2,
                    removalTime = persistentObject0.removalTime,
                    typedValue = value,
                    typedValueClass = if (value != null) value::class.java.canonicalName else null
            )

            map.create(persistentObject.id, persistentObject).blockingGet()

        } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE)
                || historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE)) {

            val map = getHistoryMap(OptimizedHistoricVariableInstanceEntity::class.java)
            val historicVariableInstanceEntity = map.get(historyEvent.variableInstanceId).blockingGet()
            checkNotNull(historicVariableInstanceEntity) {
                "Unable to find historic variable instance with ID: ${historyEvent.variableInstanceId}.  ${historyEvent}"
            }

            val persistentObject0 = HistoricVariableInstanceEntity(historyEvent)
            persistentObject0.state = HistoricVariableInstance.STATE_CREATED

            val persistentObject = OptimizedHistoricVariableInstanceEntity(
                    state = persistentObject0.state,
                    id = persistentObject0.id,
                    processDefinitionKey = persistentObject0.processDefinitionKey,
                    processDefinitionId = persistentObject0.processDefinitionId,
                    rootProcessInstanceId = persistentObject0.rootProcessInstanceId,
                    processInstanceId = persistentObject0.processInstanceId,
                    taskId = persistentObject0.taskId,
                    executionId = persistentObject0.executionId,
                    activityInstanceId = persistentObject0.activityInstanceId,
                    tenantId = persistentObject0.tenantId,
                    caseDefinitionKey = persistentObject0.caseDefinitionKey,
                    caseDefinitionId = persistentObject0.caseDefinitionId,
                    caseInstanceId = persistentObject0.caseInstanceId,
                    caseExecutionId = persistentObject0.caseExecutionId,
                    name = persistentObject0.variableName,
                    revision = persistentObject0.revision,
                    createTime = persistentObject0.createTime,
                    longValue = persistentObject0.longValue,
                    doubleValue = persistentObject0.doubleValue,
                    textValue = persistentObject0.textValue,
                    textValue2 = persistentObject0.textValue2,
                    removalTime = persistentObject0.removalTime,
                    typedValue = persistentObject0.getTypedValue(true).value,
                    typedValueClass = persistentObject0.typedValue.type.name)

            map.update(persistentObject.id, persistentObject).blockingGet()

        } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_DELETE)) {
            val map = getHistoryMap(HistoricVariableInstanceEntity::class.java)

            val historicVariableInstanceEntity = map.get(historyEvent.variableInstanceId).blockingGet()

            checkNotNull(historicVariableInstanceEntity) {
                "Unable to find historic variable instance with ID: ${historyEvent.variableInstanceId}.  ${historyEvent}"
            }

            historicVariableInstanceEntity.state = HistoricVariableInstance.STATE_DELETED

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

            if (historyEvent.id == null){
                historyEvent.id = (processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl).idGenerator.nextId
            }
            map.create(historyEvent.id, historyEvent).blockingGet()

        } else {
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