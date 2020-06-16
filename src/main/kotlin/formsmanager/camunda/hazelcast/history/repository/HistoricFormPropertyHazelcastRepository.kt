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
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricFormPropertyEntity(key: String,
                                     classId: String,
                                     value: HistoricFormPropertyEventEntity) : GenericMapStoreEntity<HistoricFormPropertyEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricFormPropertyEntityMapStoreRepository : MapStoreCrudRepository<String, HistoricFormPropertyEntity>


@Singleton
class HistoricFormPropertyToEntityTypeConverter : TypeConverter<HistoricFormPropertyEventEntity, HistoricFormPropertyEntity> {
    override fun convert(`object`: HistoricFormPropertyEventEntity, targetType: Class<HistoricFormPropertyEntity>, context: ConversionContext): Optional<HistoricFormPropertyEntity> {
        return Optional.of(HistoricFormPropertyEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricFormPropertyHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricFormPropertyEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricFormProperty"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricFormPropertyEventEntity::class.java

    override val iMap: IMap<String, HistoricFormPropertyEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricFormPropertyEventEntity>(MAP_NAME) }
}