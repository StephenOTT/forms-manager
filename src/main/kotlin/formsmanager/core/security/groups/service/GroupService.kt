package formsmanager.core.security.groups.service

import formsmanager.core.security.groups.GroupMapKey
import formsmanager.core.security.groups.domain.GroupEntity
import formsmanager.core.security.groups.repository.GroupHazelcastRepository
import io.reactivex.Single
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
    fun createGroup(groupEntity: GroupEntity, subject: Subject? = null): Single<GroupEntity> {
        return Single.fromCallable {
            subject?.let {
                subject.checkPermission("groups:create:${groupEntity.tenant}")
            }
        }.flatMap {
            groupHazelcastRepository.create(groupEntity)
        }
    }

    /**
     * Get/find a Group
     * @param groupId Group ID
     */
    fun getGroup(groupId: GroupMapKey, subject: Subject? = null): Single<GroupEntity> {
        return groupHazelcastRepository.get(groupId.toUUID()).map { g ->
            subject?.let {
                subject.checkPermission("groups:read:${g.tenant}")
            }
            g
        }
    }

    fun groupExists(groupMapKey: GroupMapKey): Single<Boolean> {
        return groupExists(groupMapKey.toUUID())
    }

    fun groupExists(groupMapKey: UUID): Single<Boolean> {
        return groupHazelcastRepository.exists(groupMapKey)
    }

    /**
     * Update/overwrite Group
     * @param groupEntity Group to be updated/overwritten
     */
    fun updateGroup(groupEntity: GroupEntity, subject: Subject? = null): Single<GroupEntity> {
        return groupHazelcastRepository.update(groupEntity) { originalItem, newItem ->
            //Update logic for automated fields @TODO consider automation with annotations
            subject?.let {
                subject.checkPermission("groups:update:${originalItem.tenant}")
            }

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