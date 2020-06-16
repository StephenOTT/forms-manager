package formsmanager.tenants.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.core.hazelcast.map.persistence.GenericMapStoreEntity
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import formsmanager.tenants.domain.Tenant
import formsmanager.tenants.domain.TenantId
import io.micronaut.context.annotation.Context
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

@Entity
class TenantEntity(key: TenantId,
                   classId: String,
                   value: Tenant) : GenericMapStoreEntity<Tenant>(key.toMapKey(), classId, value)


@JdbcRepository(dialect = Dialect.H2)
@Context
interface TenantsMapStoreRepository : MapStoreCrudRepository<String, TenantEntity>


@Singleton
class TenantToEntityTypeConverter : TypeConverter<Tenant, TenantEntity> {
    override fun convert(`object`: Tenant, targetType: Class<TenantEntity>, context: ConversionContext): Optional<TenantEntity> {
        return Optional.of(TenantEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

@Singleton
class TenantHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, Tenant>{

    companion object {
        val MAP_NAME = "tenants"
    }

    override val iMap: IMap<String, Tenant> by lazy { hazelcastInstance.getMap<String, Tenant>(MAP_NAME) }
}