package formsmanager.tenants.service

import formsmanager.tenants.domain.TenantEntity
import formsmanager.tenants.repository.TenantHazelcastRepository
import io.reactivex.Single
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
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
        return Single.fromCallable {
            subject?.let {
                subject.checkPermission(WildcardPermission("tenants:create"))
            }
        }.flatMap {
            tenantHazelcastRepository.create(tenantEntity)
        }
    }

    /**
     * Get/find a Tenant
     * @param tenantId Tenant ID
     */
    fun getTenant(tenantId: UUID, subject: Subject? = null): Single<TenantEntity> {
        return tenantHazelcastRepository.find(tenantId).map { fe ->
            subject?.let {
                subject.checkPermission(WildcardPermission("tenants:read:${fe.owner}"))
            }

            fe
        }
    }

    fun tenantExists(tenantId: UUID, subject: Subject? = null, mustExist: Boolean = false): Single<Boolean> {
        subject?.let {
            subject.checkPermission(WildcardPermission("tenants:read:${tenantId}"))
        }
        return tenantHazelcastRepository.exists(tenantId).map {
            if (mustExist){
                throw IllegalArgumentException("Tenant $tenantId cannot be found")
            }
            it
        }
    }

    /**
     * Update/overwrite tenant
     * @param tenantEntity Tenant to be updated/overwritten
     */
    fun updateTenant(tenantEntity: TenantEntity, subject: Subject? = null): Single<TenantEntity> {
        return getTenant(tenantEntity.id).map { fe ->
            subject?.let {
                subject.checkPermission(WildcardPermission("tenants:update:${fe.owner}"))
            }
        }.flatMap {
            tenantHazelcastRepository.update(tenantEntity) { originalItem, newItem ->
                //Update logic for automated fields @TODO consider automation with annotations
                newItem.copy(
                        ol = originalItem.ol + 1,
                        id = originalItem.id,
                        createdAt = originalItem.createdAt,
                        updatedAt = Instant.now()
                )
            }
        }
    }
}