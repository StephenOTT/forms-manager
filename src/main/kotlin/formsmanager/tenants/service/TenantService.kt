package formsmanager.tenants.service

import formsmanager.core.security.checkAuthorization
import formsmanager.tenants.TenantMapKey
import formsmanager.tenants.domain.TenantEntity
import formsmanager.tenants.repository.TenantHazelcastRepository
import io.reactivex.Single
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton


@Singleton
class TenantService(
        private val tenantHazelcastRepository: TenantHazelcastRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(TenantService::class.java)
    }

    /**
     * Create/insert a Tenant
     * @param tenantEntity Tenant to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun createTenant(tenantEntity: TenantEntity, subject: Subject? = null): Single<TenantEntity> {
        return subject.checkAuthorization("tenants:create")
                .flatMap {
                    tenantHazelcastRepository.create(tenantEntity)
                }
    }

    /**
     * Get/find a Tenant
     * @param tenantId Tenant ID
     */
    fun getTenant(tenantMapKey: TenantMapKey, subject: Subject? = null): Single<TenantEntity> {
        return tenantHazelcastRepository.get(tenantMapKey.toUUID()).map { te ->
            subject?.let {
                subject.checkPermission("tenants:read:${te.internalId}")
            }
            te
        }
    }

    /**
     * Checks if tenant exists.
     * Optional mustExist parameter that will throw a IllegalArgumentException if the tenant does not exist.
     * @exception IllegalArgumentException if mustExist is set to true, and the request tenant does not exist
     */
    fun tenantExists(tenantMapKey: TenantMapKey, mustExist: Boolean = false): Single<Boolean> {
        return tenantExists(tenantMapKey.toUUID(), mustExist)
    }

    fun tenantExists(tenantName: String, mustExist: Boolean = false): Single<Boolean> {
        return tenantExists(TenantMapKey(tenantName), mustExist)
    }

    fun tenantExists(tenantMapKey: UUID, mustExist: Boolean = false): Single<Boolean> {
        return tenantHazelcastRepository.exists(tenantMapKey).map {
            if (mustExist) {
                require(it, lazyMessage = { "Tenant does not exist" })
            }
            it
        }
    }


    /**
     * Update/overwrite tenant
     * @param tenantEntity Tenant to be updated/overwritten
     */
    fun updateTenant(tenantEntity: TenantEntity, subject: Subject? = null): Single<TenantEntity> {
        return tenantHazelcastRepository.update(tenantEntity) { originalItem, newItem ->
            //Update logic for automated fields @TODO consider automation with annotations

            subject?.let {
                // @TODO review if this should be a perm based on the tenantID.
                subject.checkPermission("tenants:update:${originalItem.internalId}")
            }

            newItem.copy(
                    ol = originalItem.ol + 1,
                    internalId = originalItem.internalId,
                    createdAt = originalItem.createdAt,
                    updatedAt = Instant.now()
            )
        }
    }
}