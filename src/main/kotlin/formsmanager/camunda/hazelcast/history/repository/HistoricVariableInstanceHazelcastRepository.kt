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
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class HistoricVariableInstanceEntity(key: String,
                                     classId: String,
                                     value: HistoricVariableInstanceEntity) : GenericMapStoreEntity<HistoricVariableInstanceEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface HistoricHistoricVariableMapStoreRepository : MapStoreCrudRepository<String, formsmanager.camunda.hazelcast.history.repository.HistoricVariableInstanceEntity>


@Singleton
class HistoricHistoricVariableInstanceToEntityTypeConverter : TypeConverter<HistoricVariableInstanceEntity, formsmanager.camunda.hazelcast.history.repository.HistoricVariableInstanceEntity> {
    override fun convert(`object`: HistoricVariableInstanceEntity, targetType: Class<formsmanager.camunda.hazelcast.history.repository.HistoricVariableInstanceEntity>, context: ConversionContext): Optional<formsmanager.camunda.hazelcast.history.repository.HistoricVariableInstanceEntity> {

        return Optional.of(HistoricVariableInstanceEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class HistoricVariableInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, HistoricVariableInstanceEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricVariableInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = HistoricVariableInstanceEntity::class.java

    override val iMap: IMap<String, HistoricVariableInstanceEntity> by lazy { hazelcastInstance.getMap<String, HistoricVariableInstanceEntity>(MAP_NAME) }
}