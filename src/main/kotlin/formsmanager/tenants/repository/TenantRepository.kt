package formsmanager.tenants.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.hazelcast.annotation.MapStore
import formsmanager.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.hazelcast.map.persistence.CurdableMapStore
import formsmanager.hazelcast.map.HazelcastCrudRepository
import formsmanager.hazelcast.map.persistence.MapStoreItemWrapperEntity
import formsmanager.tenants.domain.TenantEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for TenantEntity
 */
@Entity
class TenantEntityWrapper(key: UUID,
                        classId: String,
                        value: TenantEntity) : MapStoreItemWrapperEntity<TenantEntity>(key, classId, value)

/**
 * JDBC Repository for use by the Tenants MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface TenantsMapStoreRepository : CrudableMapStoreRepository<TenantEntityWrapper>

/**
 * Provides a MapStore implementation for TenantEntity
 */
@Singleton
class TenantsMapStore(mapStoreRepository: TenantsMapStoreRepository) :
        CurdableMapStore<TenantEntity, TenantEntityWrapper, TenantsMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Tenant IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(TenantsMapStore::class, TenantHazelcastRepository.MAP_NAME)
class TenantHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<UUID, TenantEntity>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

        companion object{
                const val MAP_NAME = "tenants"
        }
}