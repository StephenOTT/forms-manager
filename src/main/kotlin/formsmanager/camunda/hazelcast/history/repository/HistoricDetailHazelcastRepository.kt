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
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricDetailEntity(key: String,
                                     classId: String,
                                     value: HistoricDetailEventEntity) : GenericMapStoreEntity<HistoricDetailEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricDetailEventMapStoreRepository : MapStoreCrudRepository<String, HistoricDetailEntity>


@Singleton
class HistoricDetailEventToEntityTypeConverter : TypeConverter<HistoricDetailEventEntity, HistoricDetailEntity> {
    override fun convert(`object`: HistoricDetailEventEntity, targetType: Class<HistoricDetailEntity>, context: ConversionContext): Optional<HistoricDetailEntity> {
        return Optional.of(HistoricDetailEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricDetailHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricDetailEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricDetail"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricDetailEventEntity::class.java

    override val iMap: IMap<String, HistoricDetailEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricDetailEventEntity>(MAP_NAME) }
}