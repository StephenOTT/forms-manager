package formsmanager.core.security.roles.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.core.security.roles.domain.Role
import formsmanager.core.security.roles.domain.RoleId
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for RoleEntity
 */
@Entity
class RoleEntity(key: RoleId,
                 classId: String,
                 value: Role) : MapStoreEntity<Role>(key.toMapKey(), classId, value)

/**
 * JDBC Repository for use by the Roles MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface RolesMapStoreRepository : CrudableMapStoreRepository<RoleEntity>

/**
 * Provides a MapStore implementation for RoleEntity
 */
@Singleton
class RolesMapStore(mapStoreRepository: RolesMapStoreRepository) :
        CurdableMapStore<Role, RoleEntity, RolesMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Role IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(RolesMapStore::class, RoleHazelcastRepository.MAP_NAME)
class RoleHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<Role>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "roles"
        }
}