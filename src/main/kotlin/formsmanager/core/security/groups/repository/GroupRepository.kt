package formsmanager.core.security.groups.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.core.security.groups.domain.Group
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for GroupEntity
 */
@Entity
class GroupEntity(key: CrudableObjectId<*>,
                  classId: String,
                  value: Group) : MapStoreEntity<Group>(key.toMapKey(), classId, value)

/**
 * JDBC Repository for use by the Groups MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface GroupsMapStoreRepository : CrudableMapStoreRepository<GroupEntity>

/**
 * Provides a MapStore implementation for GroupEntity
 */
@Singleton
class GroupsMapStore(mapStoreRepository: GroupsMapStoreRepository) :
        CurdableMapStore<Group, GroupEntity, GroupsMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Group IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(GroupsMapStore::class, GroupHazelcastRepository.MAP_NAME)
class GroupHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<Group>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "groups"
        }
}