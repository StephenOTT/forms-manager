package formsmanager.camunda.hazelcast.history.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.core.hazelcast.map.persistence.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import io.micronaut.context.annotation.Context
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import org.camunda.bpm.engine.impl.db.HistoricEntity
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricDecisionInstanceEntity(key: String,
                                     classId: String,
                                     value: HistoricDecisionInstanceEntity) : GenericMapStoreEntity<HistoricDecisionInstanceEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricDecisionInstanceMapStoreRepository : MapStoreCrudRepository<String, formsmanager.camunda.hazelcast.history.repository.HistoricDecisionInstanceEntity>


@Singleton
class HistoricDecisionInstanceToEntityTypeConverter : TypeConverter<HistoricDecisionInstanceEntity, formsmanager.camunda.hazelcast.history.repository.HistoricDecisionInstanceEntity> {
    override fun convert(`object`: HistoricDecisionInstanceEntity, targetType: Class<formsmanager.camunda.hazelcast.history.repository.HistoricDecisionInstanceEntity>, context: ConversionContext): Optional<formsmanager.camunda.hazelcast.history.repository.HistoricDecisionInstanceEntity> {
        return Optional.of(formsmanager.camunda.hazelcast.history.repository.HistoricDecisionInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricDecisionInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricDecisionInstanceEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricDecisionInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricDecisionInstanceEntity::class.java

    override val iMap: IMap<String, HistoricDecisionInstanceEntity> by lazy { hazelcastInstance.getMap<String, HistoricDecisionInstanceEntity>(MAP_NAME) }
}