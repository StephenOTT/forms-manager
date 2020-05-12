package formsmanager.users.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.users.domain.User
import formsmanager.users.domain.UserId
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Single
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for User Entity
 */
@Entity
class UserEntity(key: UserId,
                 classId: String,
                 value: User) : MapStoreEntity<User>(key.toMapKey(), classId, value)

/**
 * JDBC Repository for use by the Users MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface UsersMapStoreRepository : CrudableMapStoreRepository<UserEntity>

/**
 * Provides a MapStore implementation for User Entity
 */
@Singleton
class UsersMapStore(mapStoreRepository: UsersMapStoreRepository) :
        CurdableMapStore<User, UserEntity, UsersMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Users IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(UsersMapStore::class, UsersHazelcastRepository.MAP_NAME)
class UsersHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<User>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

    companion object {
        const val MAP_NAME = "users"
    }

    fun isActive(userMapKey: UserId): Single<Boolean> {
        return get(userMapKey).map {
            it.accountActive()
        }
    }

}