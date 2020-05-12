package formsmanager.tenants.service

import com.hazelcast.projection.Projections
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.security.shiro.checkAuthorization
import formsmanager.tenants.domain.Tenant
import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.repository.TenantHazelcastRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.inject.Singleton


fun Set<TenantId>.getTenants(service: TenantService, subject: Subject? = null): Single<Set<Tenant>> {
    return service.get(this, subject).map {
        it.toSet()
    }
}

fun TenantId.getTenant(service: TenantService, subject: Subject? = null): Single<Tenant> {
    return service.get(this, subject)
}


@Singleton
class TenantService(
        private val tenantHazelcastRepository: TenantHazelcastRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(TenantService::class.java)
    }

    /**
     * Create/insert a Tenant
     * @param tenant Tenant to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun create(tenant: Tenant, subject: Subject? = null): Single<Tenant> {
        return subject.checkAuthorization("tenants:create")
                .flatMap {
                    tenantHazelcastRepository.create(tenant)
                }
    }

    /**
     * Get/find a Tenant
     * @param tenantId Tenant ID
     */
    fun get(id: TenantId, subject: Subject? = null): Single<Tenant> {
        return tenantHazelcastRepository.get(id).flatMap { te ->
            subject.checkAuthorization("tenants:read:${te.id}").map {
                te
            }
        }
    }

    fun get(ids: Set<TenantId>, subject: Subject? = null): Single<List<Tenant>> {
        return tenantHazelcastRepository.get(ids).map { items ->
            items.forEach { item ->
                subject.checkAuthorization("tenants:read:${item.id}")
                        .subscribeOn(Schedulers.io()).blockingGet()
            }
            items
        }
    }


    fun getByName(tenantName: String, subject: Subject? = null): Single<Tenant> {
        return tenantHazelcastRepository.get(Predicate {
            it.value.name == tenantName
        }).flatMap { tenant ->
            subject.checkAuthorization("tenants:read:${tenant.id}").map {
                tenant
            }
        }
    }

    /**
     * Optimized function to get the TenantId from a Tenant Name.
     * Primarily used for HTTP tenant exist checks which is a common request
     * @exception NoSuchElementException if tenant name could not be found
     */
    fun getTenantIdByTenantName(tenantName: String): Single<TenantId>{
        return Single.fromCallable {
            tenantHazelcastRepository.mapService.project(
                    Projections.singleAttribute<MutableMap.MutableEntry<String, Tenant>, TenantId>("id"),
                    Predicates.equal("name", tenantName)
            ).single()
        }
    }

    fun exists(tenantMapKey: TenantId, mustExist: Boolean = false): Single<Boolean> {
        return tenantHazelcastRepository.exists(tenantMapKey).map {
            if (mustExist) {
                require(it, lazyMessage = { "Tenant does not exist" })
            }
            it
        }
    }


    /**
     * Update/overwrite tenant
     * @param tenant Tenant to be updated/overwritten
     */
    fun update(tenant: Tenant, subject: Subject? = null): Single<Tenant> {
        return tenantHazelcastRepository.update(tenant) { originalItem, newItem ->
            //Update logic for automated fields @TODO consider automation with annotations

            // @TODO review if this should be a perm based on the tenantID.
            subject.checkAuthorization("tenants:update:${originalItem.id}")
                    .subscribeOn(Schedulers.io()).blockingGet()

            newItem.copy(
                    ol = originalItem.ol + 1,
                    id = originalItem.id,
                    createdAt = originalItem.createdAt,
                    updatedAt = Instant.now()
            )
        }
    }
}