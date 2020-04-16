package formsmanager.respository

import com.hazelcast.core.HazelcastInstance
import formsmanager.domain.FormEntity
import formsmanager.hazelcast.annotation.MapStore
import formsmanager.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.hazelcast.map.persistence.CurdableMapStore
import formsmanager.hazelcast.map.HazelcastCrudRepository
import formsmanager.hazelcast.map.persistence.MapStoreItemWrapperEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for FormEntity
 */
@Entity
class FormEntityWrapper(key: UUID,
                        classId: String,
                        value: FormEntity) : MapStoreItemWrapperEntity<FormEntity>(key, classId, value)

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
        private val hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<UUID, FormEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "forms"
        }
}