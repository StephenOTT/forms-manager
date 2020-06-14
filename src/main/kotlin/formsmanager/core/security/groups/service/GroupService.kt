package formsmanager.core.security.groups.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.hazelcast.query.PagingUtils
import formsmanager.core.hazelcast.query.beanDescription
import formsmanager.core.hazelcast.query.predicate.SecurityPredicate
import formsmanager.core.security.groups.domain.Group
import formsmanager.core.security.groups.domain.GroupId
import formsmanager.core.security.groups.repository.GroupHazelcastRepository
import formsmanager.core.security.shiro.checkAuthorization
import formsmanager.forms.domain.FormSchema
import formsmanager.tenants.domain.TenantId
import io.micronaut.context.ApplicationContext
import io.micronaut.data.model.Pageable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.inject.Singleton


fun Set<GroupId>.getGroups(groupService: GroupService, subject: Subject? = null): Single<Set<Group>> {
    return groupService.get(this, subject).map {
        it.toSet()
    }
}

fun GroupId.getGroup(groupService: GroupService, subject: Subject? = null): Single<Group> {
    return groupService.get(this, subject)
}


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
     * @param item Group to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun create(item: Group, subject: Subject? = null): Single<Group> {
        return subject.checkAuthorization("groups:create:${item.tenant.asString()}").flatMap {
            groupHazelcastRepository.create(item.id.toMapKey(), item)
        }
    }

    fun get(id: GroupId, subject: Subject? = null): Single<Group> {
        return groupHazelcastRepository.get(id.toMapKey()).map { g ->
            subject.checkAuthorization("groups:read:${g.tenant.asString()}").map {
                g
            }
            g
        }
    }

    fun getByName(name: String, tenantId: TenantId, subject: Subject? = null): Single<Group>{
        return groupHazelcastRepository.get(Predicate {
            it.value.tenant == tenantId && it.value.name == name
        })
    }

    /**
     * Gets multiple groups by group MapKeys.
     * If subject is provided, then subject must have permissions to read all provided group mapkeys.
     */
    fun get(idSet: Set<GroupId>, subject: Subject? = null): Single<List<Group>> {
        return groupHazelcastRepository.get(idSet.map { it.toMapKey() }.toSet()).map { groups ->
            groups.forEach { group ->
                subject.checkAuthorization("groups:read:${group.tenant.asString()}")
                        .subscribeOn(Schedulers.io()).blockingGet()
            }
            groups
        }
    }

    fun exists(id: GroupId): Single<Boolean> {
        return groupHazelcastRepository.exists(id.toMapKey())
    }

    /**
     * Update/overwrite Group
     * @param item Group to be updated/overwritten
     */
    fun update(item: Group, subject: Subject? = null): Single<Group> {
        return groupHazelcastRepository.update(item.id.toMapKey(), item) { originalItem, newItem ->
            //Update logic for automated fields @TODO consider automation with annotations
            subject.checkAuthorization("groups:update:${originalItem.tenant.asString()}")
                    .subscribeOn(Schedulers.io()).blockingGet()

            newItem.copy(
                    ol = originalItem.ol + 1,
                    id = originalItem.id,
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
    fun search(predicate: Predicate<String, Group>, pageable: Pageable, subject: Subject?): Flowable<Group> {

        return Flowable.fromCallable {

            val finalPred: Predicate<String, Group> =
                    if (subject != null) {
                        val secPred = SecurityPredicate<Group>(subject, Logical.AND) {
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
            val beanDesc = mapper.beanDescription<FormSchema>()

            // Build comparators lists
            val comparators = PagingUtils.createPagingPredicateComparators<String, Group>(beanDesc, pageable)

            PagingUtils.createPagingPredicate(
                    finalPred,
                    comparators,
                    if (pageable.size == 0) 10 else pageable.size, // @TODO review: this is currently a fall back, to say that results are always paged, regardless.
                    pageable.number
            )

        }.flatMapIterable {
            groupHazelcastRepository.iMap.values(it)
        }
    }
}