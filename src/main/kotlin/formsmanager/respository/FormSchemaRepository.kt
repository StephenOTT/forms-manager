package formsmanager.respository

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicates
import formsmanager.domain.FormSchemaEntity
import formsmanager.hazelcast.query.PagingUtils.Companion.createPagingPredicate
import formsmanager.hazelcast.query.PagingUtils.Companion.createPagingPredicateComparators
import formsmanager.hazelcast.annotation.MapStore
import formsmanager.hazelcast.map.HazelcastCrudRepository
import formsmanager.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.hazelcast.map.persistence.CurdableMapStore
import formsmanager.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.hazelcast.query.beanDescription
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


/**
 * Implementation providing a Form Schema IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(FormSchemasMapStore::class, FormSchemaHazelcastRepository.MAP_NAME)
class FormSchemaHazelcastRepository(
        hazelcastInstance: HazelcastInstance,
        private val mapper: ObjectMapper
) :
        HazelcastCrudRepository<UUID, FormSchemaEntity>(
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
    fun getSchemasForForm(formId: UUID, pageable: Pageable = Pageable.from(0)): Flowable<FormSchemaEntity> {
        return Flowable.fromCallable {
            // Gets the jackson BeanDescription for the entity
            val beanDesc = mapper.beanDescription<FormSchemaEntity>()

            val comparators = createPagingPredicateComparators<UUID, FormSchemaEntity>(beanDesc, pageable)
            createPagingPredicate(
                    Predicates.equal("formId", formId),
                    comparators,
                    pageable.size,
                    pageable.number
            )

        }.flatMapIterable {
            mapService.values(it)
        }
    }
}


/**
 * Entity for storage in a IMDG MapStore for FormSchemaEntity
 */
@Entity
class FormSchemaEntityWrapper(key: UUID,
                              classId: String,
                              value: FormSchemaEntity) : MapStoreItemWrapperEntity<FormSchemaEntity>(key, classId, value)

/**
 * JDBC Repository for use by the FormSchemas MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface FormSchemasMapStoreRepository : CrudableMapStoreRepository<FormSchemaEntityWrapper>

/**
 * Provides a MapStore implementation for FormSchemaEntity
 */
@Singleton
class FormSchemasMapStore(mapStoreRepository: FormSchemasMapStoreRepository) :
        CurdableMapStore<FormSchemaEntity, FormSchemaEntityWrapper, FormSchemasMapStoreRepository>(mapStoreRepository)