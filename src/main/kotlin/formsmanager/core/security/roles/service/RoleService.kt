package formsmanager.core.security.roles.service

import formsmanager.core.security.roles.RoleMapKey
import formsmanager.core.security.roles.domain.RoleEntity
import formsmanager.core.security.roles.repository.RoleHazelcastRepository
import io.reactivex.Single
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton

/**
 * Primary Entry point for working with Roles
 * The Role Service provides the various functions for BREAD of Roles.
 */
@Singleton
class RoleService(
        private val roleHazelcastRepository: RoleHazelcastRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(RoleService::class.java)
    }

    /**
     * Create/insert a Role
     * @param entity Role to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun create(entity: RoleEntity, subject: Subject? = null): Single<RoleEntity> {
        return Single.fromCallable {
            subject?.let {
                subject.checkPermission("roles:create:${entity.tenant}")
            }
        }.flatMap {
            roleHazelcastRepository.create(entity)
        }
    }

    /**
     * Get/find a Role
     * @param id Role ID
     */
    fun get(roleMapKey: RoleMapKey, subject: Subject? = null): Single<RoleEntity> {
        return roleHazelcastRepository.get(roleMapKey.toUUID()).map { g ->
            subject?.let {
                subject.checkPermission("roles:read:${g.tenant}")
            }
            g
        }
    }

    fun roleExists(roleMapKey: RoleMapKey): Single<Boolean> {
        return roleHazelcastRepository.exists(roleMapKey.toUUID())
    }

    /**
     * Update/overwrite Role
     * @param entity Role to be updated/overwritten
     */
    fun update(entity: RoleEntity, subject: Subject? = null): Single<RoleEntity> {
        return roleHazelcastRepository.update(entity) { originalItem, newItem ->
            subject?.let {
                subject.checkPermission("roles:update:${originalItem.tenant}")
            }

            //Update logic for automated fields @TODO consider automation with annotations
            newItem.copy(
                    ol = originalItem.ol + 1,
                    internalId = originalItem.internalId,
                    tenant = originalItem.tenant,
                    createdAt = originalItem.createdAt,
                    updatedAt = Instant.now()
            )
        }
    }
}