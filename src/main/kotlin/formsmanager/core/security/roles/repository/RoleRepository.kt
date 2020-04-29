package formsmanager.core.security.roles.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.core.security.roles.RoleMapKey
import formsmanager.core.security.roles.domain.RoleEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for RoleEntity
 */
@Entity
class RoleEntityWrapper(key: RoleMapKey,
                        classId: String,
                        value: RoleEntity) : MapStoreItemWrapperEntity<RoleEntity>(key.toUUID(), classId, value)

/**
 * JDBC Repository for use by the Roles MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface RolesMapStoreRepository : CrudableMapStoreRepository<RoleEntityWrapper>

/**
 * Provides a MapStore implementation for RoleEntity
 */
@Singleton
class RolesMapStore(mapStoreRepository: RolesMapStoreRepository) :
        CurdableMapStore<RoleEntity, RoleEntityWrapper, RolesMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Role IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(RolesMapStore::class, RoleHazelcastRepository.MAP_NAME)
class RoleHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<RoleEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "roles"
        }
}