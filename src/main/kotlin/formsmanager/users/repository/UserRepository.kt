package formsmanager.users.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicates
import formsmanager.hazelcast.annotation.MapStore
import formsmanager.hazelcast.map.HazelcastCrudRepository
import formsmanager.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.hazelcast.map.persistence.CurdableMapStore
import formsmanager.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.users.domain.UserEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Maybe
import io.reactivex.Single
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for User Entity
 */
@Entity
class UserEntityWrapper(key: UUID,
                        classId: String,
                        value: UserEntity) : MapStoreItemWrapperEntity<UserEntity>(key, classId, value)

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
        HazelcastCrudRepository<UUID, UserEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

    companion object {
        const val MAP_NAME = "users"
    }

    //@TODO add index for email address

    fun userExists(email: String, tenant: UUID): Single<Boolean> {
        //@TODO Create a Map Index for Email and Tenant
        return Single.fromCallable {
            mapService.values(Predicates.and(
                    Predicates.equal<String, UUID>("emailInfo.email", email),
                    Predicates.equal<String, UUID>("tenant", tenant))
            ).size == 1
        }
    }

    fun findByEmail(email: String, tenant: UUID): Single<UserEntity> {
        //@TODO Create a Map Index for Email and Tenant
        return Single.fromCallable {
            mapService.values(Predicates.and(
                    Predicates.equal<String, UUID>("emailInfo.email", email),
                    Predicates.equal<String, UUID>("tenant", tenant))
            ).single()
        }
    }

    /**
     * Eval if the user account is active.  Active is a aggregate of multiple account rules.
     */
    fun isActive(userId: UUID): Single<Boolean> {
        return find(userId).map {
            it.accountActive()
        }
    }

}