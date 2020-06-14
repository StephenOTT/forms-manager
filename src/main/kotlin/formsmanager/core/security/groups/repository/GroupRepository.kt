package formsmanager.core.security.groups.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.engine.history.mapstore.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import formsmanager.core.security.groups.domain.Group
import formsmanager.core.security.groups.domain.GroupId
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity


@Entity
class GroupEntity(key: GroupId,
                  classId: String,
                  value: Group) : GenericMapStoreEntity<Group>(key.toMapKey(), classId, value)


@JdbcRepository(dialect = Dialect.H2)
interface GroupsMapStoreRepository : MapStoreCrudRepository<String, GroupEntity>

@Singleton
class GroupToEntityTypeConverter : TypeConverter<Group, GroupEntity> {
        override fun convert(`object`: Group, targetType: Class<GroupEntity>, context: ConversionContext): Optional<GroupEntity> {
                return Optional.of(GroupEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
        }
}

@Singleton
class GroupHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, Group>{

        companion object {
                val MAP_NAME = "groups"
        }

        override val iMap: IMap<String, Group> by lazy { hazelcastInstance.getMap<String, Group>(MAP_NAME) }
}