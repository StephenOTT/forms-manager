package formsmanager.groups.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.groups.domain.GroupEntity
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
        HazelcastCrudRepository<UUID, GroupEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "groups"
        }
}