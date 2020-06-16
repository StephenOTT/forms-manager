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
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricActivityInstanceEntity(key: String,
                                     classId: String,
                                     value: HistoricActivityInstanceEventEntity) : GenericMapStoreEntity<HistoricActivityInstanceEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricActivityInstanceEventMapStoreRepository : MapStoreCrudRepository<String, HistoricActivityInstanceEntity>


@Singleton
class HistoricActivityInstanceEventToEntityTypeConverter : TypeConverter<HistoricActivityInstanceEventEntity, HistoricActivityInstanceEntity> {
    override fun convert(`object`: HistoricActivityInstanceEventEntity, targetType: Class<HistoricActivityInstanceEntity>, context: ConversionContext): Optional<HistoricActivityInstanceEntity> {
        return Optional.of(HistoricActivityInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricActivityInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricActivityInstanceEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricActivityInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricActivityInstanceEventEntity::class.java

    override val iMap: IMap<String, HistoricActivityInstanceEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricActivityInstanceEventEntity>(MAP_NAME) }
}