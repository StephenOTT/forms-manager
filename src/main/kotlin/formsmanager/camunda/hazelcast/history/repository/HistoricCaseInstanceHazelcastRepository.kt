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
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricCaseInstanceEntity(key: String,
                                     classId: String,
                                     value: HistoricCaseInstanceEventEntity) : GenericMapStoreEntity<HistoricCaseInstanceEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricCaseInstanceEventMapStoreRepository : MapStoreCrudRepository<String, HistoricCaseInstanceEntity>


@Singleton
class HistoricCaseInstanceEventToEntityTypeConverter : TypeConverter<HistoricCaseInstanceEventEntity, HistoricCaseInstanceEntity> {
    override fun convert(`object`: HistoricCaseInstanceEventEntity, targetType: Class<HistoricCaseInstanceEntity>, context: ConversionContext): Optional<HistoricCaseInstanceEntity> {
        return Optional.of(HistoricCaseInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricCaseInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricCaseInstanceEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricCaseInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricCaseInstanceEventEntity::class.java

    override val iMap: IMap<String, HistoricCaseInstanceEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricCaseInstanceEventEntity>(MAP_NAME) }
}