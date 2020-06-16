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
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricUserOperationLogEntryEntity(key: String,
                                          classId: String,
                                          value: UserOperationLogEntryEventEntity) : GenericMapStoreEntity<UserOperationLogEntryEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricUserOperationLogMapStoreRepository : MapStoreCrudRepository<String, HistoricUserOperationLogEntryEntity>


@Singleton
class HistoricUserOperationLogEntryEventToEntityTypeConverter : TypeConverter<UserOperationLogEntryEventEntity, HistoricUserOperationLogEntryEntity> {
    override fun convert(`object`: UserOperationLogEntryEventEntity, targetType: Class<HistoricUserOperationLogEntryEntity>, context: ConversionContext): Optional<HistoricUserOperationLogEntryEntity> {
        return Optional.of(HistoricUserOperationLogEntryEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricUserOperationLogHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, UserOperationLogEntryEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricUserOperationLog"
    }

    override val handlerFor: Class<out HistoricEntity> = UserOperationLogEntryEventEntity::class.java

    override val iMap: IMap<String, UserOperationLogEntryEventEntity> by lazy { hazelcastInstance.getMap<String, UserOperationLogEntryEventEntity>(MAP_NAME) }
}