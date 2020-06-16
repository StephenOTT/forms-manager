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
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricJobLogEntity(key: String,
                           classId: String,
                           value: HistoricJobLogEventEntity) : GenericMapStoreEntity<HistoricJobLogEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricJobLogMapStoreRepository : MapStoreCrudRepository<String, HistoricJobLogEntity>


@Singleton
class HistoricJobLogEventToEntityTypeConverter : TypeConverter<HistoricJobLogEventEntity, HistoricJobLogEntity> {
    override fun convert(`object`: HistoricJobLogEventEntity, targetType: Class<HistoricJobLogEntity>, context: ConversionContext): Optional<HistoricJobLogEntity> {
        return Optional.of(HistoricJobLogEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricJobLogHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricJobLogEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricJobLog"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricJobLogEventEntity::class.java

    override val iMap: IMap<String, HistoricJobLogEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricJobLogEventEntity>(MAP_NAME) }
}