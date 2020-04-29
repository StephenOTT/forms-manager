package formsmanager.users.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicates
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.users.UserMapKey
import formsmanager.users.domain.UserEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Single
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for User Entity
 */
@Entity
class UserEntityWrapper(key: UserMapKey,
                        classId: String,
                        value: UserEntity) : MapStoreItemWrapperEntity<UserEntity>(key.toUUID(), classId, value)

/**
 * JDBC Repository for use by the Users MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface UsersMapStoreRepository : CrudableMapStoreRepository<UserEntityWrapper>

/**
 * Provides a MapStore implementation for User Entity
 */
@Singleton
class UsersMapStore(mapStoreRepository: UsersMapStoreRepository) :
        CurdableMapStore<UserEntity, UserEntityWrapper, UsersMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Users IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(UsersMapStore::class, UsersHazelcastRepository.MAP_NAME)
class UsersHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<UserEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

    companion object {
        const val MAP_NAME = "users"
    }

    //@TODO add index for email address

//    fun userExists(email: String, tenant: UUID): Single<Boolean> {
//        //@TODO Create a Map Index for Email and Tenant
//        return Single.fromCallable {
//            mapService.values(Predicates.and(
//                    Predicates.equal<String, UUID>("emailInfo.email", email),
//                    Predicates.equal<String, UUID>("tenant", tenant))
//            ).size == 1
//        }
//    }
//
    fun isActive(userMapKey: UUID): Single<Boolean> {
        return get(userMapKey).map {
            it.accountActive()
        }
    }

}