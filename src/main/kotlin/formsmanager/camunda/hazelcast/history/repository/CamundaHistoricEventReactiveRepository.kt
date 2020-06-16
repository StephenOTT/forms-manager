package formsmanager.camunda.hazelcast.history.repository

import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import org.camunda.bpm.engine.impl.db.HistoricEntity

interface CamundaHistoricEventReactiveRepository<K : Any, V : Any>: HazelcastReactiveRepository<K, V>{
    val handlerFor: Class<out HistoricEntity>
}