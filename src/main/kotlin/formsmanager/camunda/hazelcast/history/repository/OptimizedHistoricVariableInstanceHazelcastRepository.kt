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
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class OptimizedHistoricVariableInstanceEntity(key: String,
                                     classId: String,
                                     value: formsmanager.camunda.OptimizedHistoricVariableInstanceEntity) : GenericMapStoreEntity<formsmanager.camunda.OptimizedHistoricVariableInstanceEntity>(key, classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface OptimizedHistoricHistoricVariableMapStoreRepository : MapStoreCrudRepository<String, OptimizedHistoricVariableInstanceEntity>


@Singleton
class OptimizedHistoricHistoricVariableInstanceToEntityTypeConverter1 : TypeConverter<formsmanager.camunda.OptimizedHistoricVariableInstanceEntity, OptimizedHistoricVariableInstanceEntity> {
    override fun convert(`object`: formsmanager.camunda.OptimizedHistoricVariableInstanceEntity, targetType: Class<OptimizedHistoricVariableInstanceEntity>, context: ConversionContext): Optional<OptimizedHistoricVariableInstanceEntity> {
        return Optional.of(OptimizedHistoricVariableInstanceEntity(`object`.id!!, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class OptimizedHistoricVariableInstanceHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : CamundaHistoricEventReactiveRepository<String, formsmanager.camunda.OptimizedHistoricVariableInstanceEntity> {

    companion object {
        val MAP_NAME = "camunda-history-HistoricVariableInstance"
    }

    override val handlerFor: Class<out HistoricEntity> = formsmanager.camunda.OptimizedHistoricVariableInstanceEntity::class.java

    override val iMap: IMap<String, formsmanager.camunda.OptimizedHistoricVariableInstanceEntity> by lazy { hazelcastInstance.getMap<String, formsmanager.camunda.OptimizedHistoricVariableInstanceEntity>(MAP_NAME) }
}