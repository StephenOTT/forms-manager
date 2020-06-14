package formsmanager.forms.respository

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import com.hazelcast.query.Predicates
import formsmanager.camunda.engine.history.mapstore.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import formsmanager.core.hazelcast.query.PagingUtils.Companion.createPagingPredicate
import formsmanager.core.hazelcast.query.PagingUtils.Companion.createPagingPredicateComparators
import formsmanager.core.hazelcast.query.beanDescription
import formsmanager.forms.domain.FormId
import formsmanager.forms.domain.FormSchema
import formsmanager.forms.domain.FormSchemaId
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Flowable
import java.util.*
import javax.inject.Named
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class FormSchemaEntity(key: FormSchemaId,
                       classId: String,
                       value: FormSchema) : GenericMapStoreEntity<FormSchema>(key.toMapKey(), classId, value)


@JdbcRepository(dialect = Dialect.H2)
interface FormSchemasMapStoreRepository : MapStoreCrudRepository<String, FormSchemaEntity>

@Singleton
class FormSchemaToEntityTypeConverter : TypeConverter<FormSchema, FormSchemaEntity> {
    override fun convert(`object`: FormSchema, targetType: Class<FormSchemaEntity>, context: ConversionContext): Optional<FormSchemaEntity> {
        return Optional.of(FormSchemaEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class FormSchemaHazelcastRepository(
        @param:Named("json") private val mapper: ObjectMapper,
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, FormSchema> {

    companion object {
        val MAP_NAME = "form-schemas"
    }

    override val iMap: IMap<String, FormSchema> by lazy { hazelcastInstance.getMap<String, FormSchema>(MAP_NAME) }

    /**
     * @param formId The UUID key of the Form
     * @param pageable A Pageable instance that provides paging and sorting instructions.  Defaults value is a pageable that limits to 10 items.
     * @return Flowable of FormSchema for the specified Form Schema
     */
    fun getSchemasForForm(formMapKey: FormId, pageable: Pageable = Pageable.from(0)): Flowable<FormSchema> {
        return Flowable.fromCallable {
            // Gets the jackson BeanDescription for the entity
            val beanDesc = mapper.beanDescription<FormSchema>()

            val comparators = createPagingPredicateComparators<String, FormSchema>(beanDesc, pageable)
            createPagingPredicate(
                    Predicates.equal("formId", formMapKey),
                    comparators,
                    pageable.size,
                    pageable.number
            )

        }.flatMapIterable {
            iMap.values(it)
        }
    }
}