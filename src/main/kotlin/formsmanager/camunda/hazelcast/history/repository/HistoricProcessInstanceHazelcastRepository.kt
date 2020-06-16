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
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricProcessInstanceEntity(key: String,
                                     classId: String,
                                     value: HistoricProcessInstanceEventEntity) : GenericMapStoreEntity<HistoricProcessInstanceEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricProcessInstanceEventMapStoreRepository : MapStoreCrudRepository<String, HistoricProcessInstanceEntity>


@Singleton
class HistoricProcessInstanceEventToEntityTypeConverter : TypeConverter<HistoricProcessInstanceEventEntity, HistoricProcessInstanceEntity> {
    override fun convert(`object`: HistoricProcessInstanceEventEntity, targetType: Class<HistoricProcessInstanceEntity>, context: ConversionContext): Optional<HistoricProcessInstanceEntity> {
        return Optional.of(HistoricProcessInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricProcessInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricProcessInstanceEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricProcessInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricProcessInstanceEventEntity::class.java

    override val iMap: IMap<String, HistoricProcessInstanceEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricProcessInstanceEventEntity>(MAP_NAME) }
}