package formsmanager.forms.respository

import com.hazelcast.core.HazelcastInstance
import formsmanager.forms.domain.Form
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.forms.domain.FormId
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for FormEntity
 */
@Entity
class FormEntity(key: FormId,
                 classId: String,
                 value: Form) : MapStoreEntity<Form>(key.toMapKey(), classId, value)

/**
 * JDBC Repository for use by the Forms MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface FormsMapStoreRepository : CrudableMapStoreRepository<FormEntity>

/**
 * Provides a MapStore implementation for FormEntity
 */
@Singleton
class FormsMapStore(mapStoreRepository: FormsMapStoreRepository) :
        CurdableMapStore<Form, FormEntity, FormsMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Form IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(FormsMapStore::class, FormHazelcastRepository.MAP_NAME)
class FormHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<Form>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "forms"
        }
}