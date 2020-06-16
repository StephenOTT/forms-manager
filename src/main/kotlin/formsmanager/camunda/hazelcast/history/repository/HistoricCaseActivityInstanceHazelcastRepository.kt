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
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricCaseActivityInstanceEntity(key: String,
                                     classId: String,
                                     value: HistoricCaseActivityInstanceEventEntity) : GenericMapStoreEntity<HistoricCaseActivityInstanceEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricCaseActivityInstanceEventMapStoreRepository : MapStoreCrudRepository<String, HistoricCaseActivityInstanceEntity>


@Singleton
class HistoricCaseActivityInstanceEventToEntityTypeConverter : TypeConverter<HistoricCaseActivityInstanceEventEntity, HistoricCaseActivityInstanceEntity> {
    override fun convert(`object`: HistoricCaseActivityInstanceEventEntity, targetType: Class<HistoricCaseActivityInstanceEntity>, context: ConversionContext): Optional<HistoricCaseActivityInstanceEntity> {
        return Optional.of(HistoricCaseActivityInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricCaseActivityInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricCaseActivityInstanceEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricCaseActivityInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricCaseActivityInstanceEventEntity::class.java

    override val iMap: IMap<String, HistoricCaseActivityInstanceEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricCaseActivityInstanceEventEntity>(MAP_NAME) }
}