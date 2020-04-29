package formsmanager.forms.respository

import com.hazelcast.core.HazelcastInstance
import formsmanager.forms.domain.FormEntity
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.forms.FormMapKey
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for FormEntity
 */
@Entity
class FormEntityWrapper(key: FormMapKey,
                        classId: String,
                        value: FormEntity) : MapStoreItemWrapperEntity<FormEntity>(key.toUUID(), classId, value)

/**
 * JDBC Repository for use by the Forms MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface FormsMapStoreRepository : CrudableMapStoreRepository<FormEntityWrapper>

/**
 * Provides a MapStore implementation for FormEntity
 */
@Singleton
class FormsMapStore(mapStoreRepository: FormsMapStoreRepository) :
        CurdableMapStore<FormEntity, FormEntityWrapper, FormsMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Form IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(FormsMapStore::class, FormHazelcastRepository.MAP_NAME)
class FormHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<FormEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "forms"
        }
}