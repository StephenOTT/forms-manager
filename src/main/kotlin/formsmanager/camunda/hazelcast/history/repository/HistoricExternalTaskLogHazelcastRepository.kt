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
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricExternalTaskLogEntity(key: String,
                                     classId: String,
                                     value: HistoricExternalTaskLogEntity) : GenericMapStoreEntity<HistoricExternalTaskLogEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricExternalTaskLogMapStoreRepository : MapStoreCrudRepository<String, formsmanager.camunda.hazelcast.history.repository.HistoricExternalTaskLogEntity>


@Singleton
class HistoricExternalTaskLogToEntityTypeConverter : TypeConverter<HistoricExternalTaskLogEntity, formsmanager.camunda.hazelcast.history.repository.HistoricExternalTaskLogEntity> {
    override fun convert(`object`: HistoricExternalTaskLogEntity, targetType: Class<formsmanager.camunda.hazelcast.history.repository.HistoricExternalTaskLogEntity>, context: ConversionContext): Optional<formsmanager.camunda.hazelcast.history.repository.HistoricExternalTaskLogEntity> {
        return Optional.of(HistoricExternalTaskLogEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricExternalTaskLogHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricExternalTaskLogEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricExternalTaskLog"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricExternalTaskLogEntity::class.java

    override val iMap: IMap<String, HistoricExternalTaskLogEntity> by lazy { hazelcastInstance.getMap<String, HistoricExternalTaskLogEntity>(MAP_NAME) }
}