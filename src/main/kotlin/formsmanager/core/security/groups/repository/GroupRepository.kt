package formsmanager.core.security.groups.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.core.security.groups.GroupMapKey
import formsmanager.core.security.groups.domain.GroupEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for GroupEntity
 */
@Entity
class GroupEntityWrapper(key: UUID,
                        classId: String,
                        value: GroupEntity) : MapStoreItemWrapperEntity<GroupEntity>(key, classId, value)

/**
 * JDBC Repository for use by the Groups MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface GroupsMapStoreRepository : CrudableMapStoreRepository<GroupEntityWrapper>

/**
 * Provides a MapStore implementation for GroupEntity
 */
@Singleton
class GroupsMapStore(mapStoreRepository: GroupsMapStoreRepository) :
        CurdableMapStore<GroupEntity, GroupEntityWrapper, GroupsMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Group IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(GroupsMapStore::class, GroupHazelcastRepository.MAP_NAME)
class GroupHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<GroupEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "groups"
        }
}