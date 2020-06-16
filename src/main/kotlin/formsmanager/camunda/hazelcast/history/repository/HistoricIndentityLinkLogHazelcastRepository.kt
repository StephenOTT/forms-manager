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
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricIdentityLinkLogEntity(key: String,
                                    classId: String,
                                    value: HistoricIdentityLinkLogEventEntity) : GenericMapStoreEntity<HistoricIdentityLinkLogEventEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricIdentityLinkLogMapStoreRepository : MapStoreCrudRepository<String, HistoricIdentityLinkLogEntity>


@Singleton
class HistoricIdentityLinkLogEventToEntityTypeConverter : TypeConverter<HistoricIdentityLinkLogEventEntity, HistoricIdentityLinkLogEntity> {
    override fun convert(`object`: HistoricIdentityLinkLogEventEntity, targetType: Class<HistoricIdentityLinkLogEntity>, context: ConversionContext): Optional<HistoricIdentityLinkLogEntity> {
        return Optional.of(HistoricIdentityLinkLogEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricIdentityLinkLogHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricIdentityLinkLogEventEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricIdentityLinkLog"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricIdentityLinkLogEventEntity::class.java

    override val iMap: IMap<String, HistoricIdentityLinkLogEventEntity> by lazy { hazelcastInstance.getMap<String, HistoricIdentityLinkLogEventEntity>(MAP_NAME) }
}