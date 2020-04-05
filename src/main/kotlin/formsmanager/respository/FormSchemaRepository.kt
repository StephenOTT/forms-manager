package formsmanager.respository

import com.hazelcast.query.Predicates
import formsmanager.domain.FormSchemaEntity
import formsmanager.hazelcast.HazelcastJetManager
import formsmanager.hazelcast.map.CrudableMapStoreRepository
import formsmanager.hazelcast.map.CurdableMapStore
import formsmanager.hazelcast.map.HazelcastCrudRepository
import formsmanager.hazelcast.map.MapStoreItemWrapperEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Single
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Implementation providing a Form Schema IMDG IMap CRUD operations repository.
 */
@Singleton
class FormSchemaHazelcastRepository(private val jetService: HazelcastJetManager) :
        HazelcastCrudRepository<UUID, FormSchemaEntity>(
                jetService = jetService,
                mapName = "form-schemas"
        ) {

    /**
     * @param itemKey The UUID key of the Form
     * @return List of FormSchema for the specified Form Schema
     */
    fun getSchemasForForm(itemKey: UUID): Single<List<FormSchemaEntity>> {
        return Single.fromCallable {
            mapService.values(Predicates.equal("formId", itemKey)).toList()
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