package formsmanager.core.security.roles.service

import com.hazelcast.query.Predicate
import formsmanager.core.security.roles.domain.Role
import formsmanager.core.security.roles.domain.RoleId
import formsmanager.core.security.roles.repository.RoleHazelcastRepository
import formsmanager.core.security.shiro.checkAuthorization
import formsmanager.tenants.domain.TenantId
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.inject.Singleton

fun Set<RoleId>.getRoles(service: RoleService, subject: Subject? = null): Single<Set<Role>> {
    return service.get(this, subject).map {
        it.toSet()
    }
}

fun RoleId.getRole(service: RoleService, subject: Subject? = null): Single<Role> {
    return service.get(this, subject)
}


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
    fun create(entity: Role, subject: Subject? = null): Single<Role> {
        return subject.checkAuthorization("roles:create:${entity.tenant}").flatMap {
            roleHazelcastRepository.create(entity)
        }
    }

    /**
     * Get/find a Role
     * @param id Role ID
     */
    fun get(roleMapKey: RoleId, subject: Subject? = null): Single<Role> {
        return roleHazelcastRepository.get(roleMapKey).flatMap { g ->
            subject.checkAuthorization("roles:read:${g.tenant}").map {
                g
            }
        }
    }

    fun get(idSet: Set<RoleId>, subject: Subject? = null): Single<List<Role>> {
        return roleHazelcastRepository.get(idSet).map { groups ->
            subject?.let {
                groups.forEach { group ->
                    subject.checkAuthorization("roles:read:${group.tenant}")
                            .subscribeOn(Schedulers.io()).blockingGet()
                }
            }
            groups
        }
    }

    fun getByName(name: String, tenant: TenantId, subject: Subject? = null): Single<Role> {
        return roleHazelcastRepository.get(Predicate {
            it.value.tenant == tenant && it.value.name == name
        })
    }

    fun roleExists(roleMapKey: RoleId): Single<Boolean> {
        return roleHazelcastRepository.exists(roleMapKey)
    }

    /**
     * Update/overwrite Role
     * @param entity Role to be updated/overwritten
     */
    fun update(entity: Role, subject: Subject? = null): Single<Role> {
        return roleHazelcastRepository.update(entity) { originalItem, newItem ->
            subject.checkAuthorization("roles:update:${originalItem.tenant}")
                    .subscribeOn(Schedulers.io()).blockingGet()

            //Update logic for automated fields @TODO consider automation with annotations
            newItem.copy(
                    ol = originalItem.ol + 1,
                    id = originalItem.id,
                    tenant = originalItem.tenant,
                    createdAt = originalItem.createdAt,
                    updatedAt = Instant.now()
            )
        }
    }
}