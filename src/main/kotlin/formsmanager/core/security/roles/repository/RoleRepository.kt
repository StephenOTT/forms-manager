package formsmanager.core.security.roles.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.core.hazelcast.map.persistence.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import formsmanager.core.security.roles.domain.Role
import formsmanager.core.security.roles.domain.RoleId
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class RoleEntity(key: RoleId,
                 classId: String,
                 value: Role) : GenericMapStoreEntity<Role>(key.toMapKey(), classId, value)

@JdbcRepository(dialect = Dialect.H2)
interface RolesMapStoreRepository : MapStoreCrudRepository<String, RoleEntity>

@Singleton
class RoleToEntityTypeConverter : TypeConverter<Role, RoleEntity> {
        override fun convert(`object`: Role, targetType: Class<RoleEntity>, context: ConversionContext): Optional<RoleEntity> {
                return Optional.of(RoleEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
        }
}

@Singleton
class RoleHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, Role>{

        companion object {
                val MAP_NAME = "roles"
        }

        override val iMap: IMap<String, Role> by lazy { hazelcastInstance.getMap<String, Role>(MAP_NAME) }
}