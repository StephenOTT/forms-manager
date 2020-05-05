package formsmanager.core.security.groups.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.hazelcast.query.PagingUtils
import formsmanager.core.hazelcast.query.beanDescription
import formsmanager.core.hazelcast.query.predicate.SecurityPredicate
import formsmanager.core.security.groups.GroupMapKey
import formsmanager.core.security.groups.domain.GroupEntity
import formsmanager.core.security.groups.repository.GroupHazelcastRepository
import formsmanager.forms.domain.FormSchemaEntity
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import io.micronaut.core.beans.BeanIntrospector
import io.micronaut.data.model.Pageable
import io.reactivex.Flowable
import io.reactivex.Single
import org.apache.shiro.authz.annotation.Logical
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
        private val groupHazelcastRepository: GroupHazelcastRepository,
        private val mapper: ObjectMapper,
        private val appCxt: ApplicationContext
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

    /**
     * See SqlPredicate for query capabilities.
     * For all results see Predicates.AlwaysTrue().
     */
    fun search(predicate: Predicate<UUID, GroupEntity>, pageable: Pageable, subject: Subject?): Flowable<GroupEntity> {

        return Flowable.fromCallable {

            val finalPred: Predicate<UUID, GroupEntity> =
                    if (subject != null) {
                        val secPred = SecurityPredicate<GroupEntity>(subject, Logical.AND) {
                            listOf(
                                    WildcardPermission("groups:read:${it.tenant}")
                            )
                        }
                        // Injection of bean due to missing code in HZ for injection of Predicates that are local (if the predicate was created through serialization then it will have injection)
                        // @ISSUE https://github.com/hazelcast/hazelcast/issues/16957
                        appCxt.inject(secPred)

                        //Combine the Security predicate with the SqlPredicate
                        Predicates.and(secPred, predicate)
                    } else {
                        // If the subject was null/not provided, then just use the provided predicate (no need for the security predicate)
                        predicate
                    }

            // Gets the jackson BeanDescription for the entity
            val beanDesc = mapper.beanDescription<FormSchemaEntity>()

            // Build comparators lists
            val comparators = PagingUtils.createPagingPredicateComparators<UUID, GroupEntity>(beanDesc, pageable)

            PagingUtils.createPagingPredicate(
                    finalPred,
                    comparators,
                    if (pageable.size == 0) 10 else pageable.size, // @TODO review: this is currently a fall back, to say that results are always paged, regardless.
                    pageable.number
            )

        }.flatMapIterable {
            groupHazelcastRepository.mapService.values(it)
        }
    }
}