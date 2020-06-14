package formsmanager.users.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.engine.history.mapstore.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import formsmanager.users.domain.User
import formsmanager.users.domain.UserId
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.reactivex.Single
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class UserEntity(key: UserId,
                 classId: String,
                 value: User) : GenericMapStoreEntity<User>(key.toMapKey(), classId, value)


@JdbcRepository(dialect = Dialect.H2)
interface UsersMapStoreRepository : MapStoreCrudRepository<String, UserEntity>

@Singleton
class UserToEntityTypeConverter : TypeConverter<User, UserEntity> {
    override fun convert(`object`: User, targetType: Class<UserEntity>, context: ConversionContext): Optional<UserEntity> {
       return Optional.of(UserEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}


@Singleton
class UsersHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, User>{

    companion object {
        val MAP_NAME = "users"
    }

    override val iMap: IMap<String, User> by lazy { hazelcastInstance.getMap<String, User>(MAP_NAME) }

    fun isActive(userId: UserId): Single<Boolean> {
        return get(userId.toMapKey()).map {
            it.accountActive()
        }
    }

}