package formsmanager.forms.respository

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicates
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.core.hazelcast.query.PagingUtils.Companion.createPagingPredicate
import formsmanager.core.hazelcast.query.PagingUtils.Companion.createPagingPredicateComparators
import formsmanager.core.hazelcast.query.beanDescription
import formsmanager.forms.domain.FormId
import formsmanager.forms.domain.FormSchema
import formsmanager.forms.domain.FormSchemaEntityId
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Flowable
import javax.inject.Singleton
import javax.persistence.Entity


/**
 * Entity for storage in a IMDG MapStore for FormSchemaEntity
 */
@Entity
class FormSchemaEntity(key: FormSchemaEntityId,
                       classId: String,
                       value: FormSchema) : MapStoreEntity<FormSchema>(key.toMapKey(), classId, value)

/**
 * JDBC Repository for use by the FormSchemas MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface FormSchemasMapStoreRepository : CrudableMapStoreRepository<FormSchemaEntity>

/**
 * Provides a MapStore implementation for FormSchemaEntity
 */
@Singleton
class FormSchemasMapStore(mapStoreRepository: FormSchemasMapStoreRepository) :
        CurdableMapStore<FormSchema, FormSchemaEntity, FormSchemasMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Form Schema IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(FormSchemasMapStore::class, FormSchemaHazelcastRepository.MAP_NAME)
class FormSchemaHazelcastRepository(
        hazelcastInstance: HazelcastInstance,
        private val mapper: ObjectMapper
) :
        HazelcastCrudRepository<FormSchema>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

    companion object {
        const val MAP_NAME = "form-schemas"
    }


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
            mapService.values(it)
        }
    }
}