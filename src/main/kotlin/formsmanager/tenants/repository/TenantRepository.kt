package formsmanager.tenants.repository

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.hazelcast.annotation.MapStore
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.tenants.domain.Tenant
import formsmanager.tenants.domain.TenantId
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import javax.inject.Singleton
import javax.persistence.Entity

/**
 * Entity for storage in a IMDG MapStore for TenantEntity
 */
@Entity
class TenantEntity(key: TenantId,
                   classId: String,
                   value: Tenant) : MapStoreEntity<Tenant>(key.toMapKey(), classId, value)

/**
 * JDBC Repository for use by the Tenants MapStore
 */
@JdbcRepository(dialect = Dialect.H2)
interface TenantsMapStoreRepository : CrudableMapStoreRepository<TenantEntity>

/**
 * Provides a MapStore implementation for TenantEntity
 */
@Singleton
class TenantsMapStore(mapStoreRepository: TenantsMapStoreRepository) :
        CurdableMapStore<Tenant, TenantEntity, TenantsMapStoreRepository>(mapStoreRepository)

/**
 * Implementation providing a Tenant IMDG IMap CRUD operations repository.
 */
@Singleton
@MapStore(TenantsMapStore::class, TenantHazelcastRepository.MAP_NAME)
class TenantHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<Tenant>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

    companion object {
        const val MAP_NAME = "tenants"
    }

}