package formsmanager.groups.service

import formsmanager.groups.domain.GroupEntity
import formsmanager.groups.repository.GroupHazelcastRepository
import io.reactivex.Single
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton

/**
 * Primary Entry point for working with Groups
 * The Group Service provides the various functions for BREAD of Groups.
 */
@Singleton
class GroupService(
        private val groupHazelcastRepository: GroupHazelcastRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(GroupService::class.java)
    }

    /**
     * Create/insert a Group
     * @param groupEntity Group to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun createGroup(groupEntity: GroupEntity, tenant: UUID, subject: Subject? = null): Single<GroupEntity> {
        return Single.fromCallable {
            require(tenant == groupEntity.tenant, lazyMessage = { "Invalid Tenant Match." })

            subject?.let {
                subject.checkPermission(WildcardPermission("groups:create:${groupEntity.owner}:${tenant}"))
            }
        }.flatMap {
            groupHazelcastRepository.create(groupEntity)
        }
    }

    /**
     * Get/find a Group
     * @param groupId Group ID
     */
    fun getGroup(groupId: UUID, tenant: UUID, subject: Subject? = null): Single<GroupEntity> {
        return groupHazelcastRepository.find(groupId).map { g ->
            require(tenant == g.tenant, lazyMessage = { "Invalid Tenant Match." })

            subject?.let {
                subject.checkPermission(WildcardPermission("groups:read:${g.owner}:${tenant}"))
            }
            g
        }
    }

    fun groupExists(groupId: UUID): Single<Boolean> {
        return groupHazelcastRepository.exists(groupId)
    }

    /**
     * Update/overwrite Group
     * @param groupEntity Group to be updated/overwritten
     */
    fun updateGroup(groupEntity: GroupEntity, tenant: UUID, subject: Subject? = null): Single<GroupEntity> {
        return getGroup(groupEntity.id, tenant).map { g ->
            require(tenant == g.tenant, lazyMessage = { "Invalid Tenant Match." })

            subject?.let {
                subject.checkPermission(WildcardPermission("groups:update:${g.owner}:${tenant}"))
            }
        }.flatMap {
            groupHazelcastRepository.update(groupEntity) { originalItem, newItem ->
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

}