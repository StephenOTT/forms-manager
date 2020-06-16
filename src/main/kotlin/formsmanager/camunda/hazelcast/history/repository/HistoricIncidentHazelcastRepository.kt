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
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricIncidentEntity(key: String,
                             classId: String,
                             value: HistoricIncidentEventEntity) : GenericMapStoreEntity<HistoricIncidentEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricIncidentMapStoreRepository : MapStoreCrudRepository<String, HistoricIncidentEntity>


@Singleton
class HistoricIncidentEventToEntityTypeConverter : TypeConverter<HistoricIncidentEventEntity, HistoricIncidentEntity> {
    override fun convert(`object`: HistoricIncidentEventEntity, targetType: Class<HistoricIncidentEntity>, context: ConversionContext): Optional<HistoricIncidentEntity> {
        return Optional.of(HistoricIncidentEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricIncidentHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricIncidentEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricIncident"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricIncidentEventEntity::class.java

    override val iMap: IMap<String, HistoricIncidentEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricIncidentEventEntity>(MAP_NAME) }
}