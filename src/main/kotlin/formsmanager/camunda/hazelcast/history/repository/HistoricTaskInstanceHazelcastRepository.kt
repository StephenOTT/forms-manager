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
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricTaskInstanceEntity(key: String,
                                 classId: String,
                                 value: HistoricTaskInstanceEventEntity) : GenericMapStoreEntity<HistoricTaskInstanceEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricTaskInstanceEventMapStoreRepository : MapStoreCrudRepository<String, HistoricTaskInstanceEntity>


@Singleton
class HistoricTaskInstanceEventToEntityTypeConverter : TypeConverter<HistoricTaskInstanceEventEntity, HistoricTaskInstanceEntity> {
    override fun convert(`object`: HistoricTaskInstanceEventEntity, targetType: Class<HistoricTaskInstanceEntity>, context: ConversionContext): Optional<HistoricTaskInstanceEntity> {
        return Optional.of(HistoricTaskInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricTaskInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricTaskInstanceEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricTaskInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricTaskInstanceEventEntity::class.java

    override val iMap: IMap<String, HistoricTaskInstanceEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricTaskInstanceEventEntity>(MAP_NAME) }
}