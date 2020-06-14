package formsmanager.forms.respository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.engine.history.mapstore.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import formsmanager.forms.domain.Form
import formsmanager.forms.domain.FormId
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class FormEntity(key: FormId,
                 classId: String,
                 value: Form) : GenericMapStoreEntity<Form>(key.toMapKey(), classId, value)

@JdbcRepository(dialect = Dialect.H2)
interface FormsMapStoreRepository : MapStoreCrudRepository<String, FormEntity>

@Singleton
class FormToEntityTypeConverter : TypeConverter<Form, FormEntity> {
        override fun convert(`object`: Form, targetType: Class<FormEntity>, context: ConversionContext): Optional<FormEntity> {
                return Optional.of(FormEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
        }
}

@Singleton
class FormHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, Form>{

        companion object {
                val MAP_NAME = "forms"
        }

        override val iMap: IMap<String, Form> by lazy { hazelcastInstance.getMap<String, Form>(MAP_NAME) }
}