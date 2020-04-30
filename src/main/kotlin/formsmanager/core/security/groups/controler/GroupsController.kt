package formsmanager.core.security.groups.controler

import com.hazelcast.internal.util.filter.Filter
import com.hazelcast.query.impl.predicates.SqlPredicate
import formsmanager.core.exception.NotFoundException
import formsmanager.core.security.groups.GroupMapKey
import formsmanager.core.security.groups.domain.GroupEntity
import formsmanager.core.security.groups.domain.GroupEntityCreator
import formsmanager.core.security.groups.domain.GroupEntityModifier
import formsmanager.core.security.groups.service.GroupService
import formsmanager.tenants.TenantMapKey
import formsmanager.tenants.domain.TenantEntity
import formsmanager.tenants.domain.TenantEntityCreator
import formsmanager.tenants.domain.TenantEntityModifier
import formsmanager.tenants.service.TenantService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.subject.Subject
import org.slf4j.Logger
import org.slf4j.LoggerFactory


//@RequiresAuthentication
@RequiresGuest
@Controller("security/{tenantName}/groups")
class GroupsController(
        private val groupService: GroupService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GroupsController::class.java)
    }

    /**
     * Get a Group.
     * @param tenantName the name of the tenant.
     * @param groupName the name of the group
     * @return The Group.
     * @exception NotFoundException Could not find group based on the group name.
     */
    @Get("/{groupName}")
    fun get(subject: Subject, @QueryValue tenantName: String, @QueryValue groupName: String): Single<HttpResponse<GroupEntity>> {
        return groupService.getGroup(GroupMapKey(groupName, tenantName), subject)
                .map {
                    HttpResponse.ok(it)
                }
    }

    fun search(){

    }

    /**
     * Create a Group.
     * @param tenantName The Tenant to be created.
     * @return the created Tenant
     */
    @Post("/")
    fun create(subject: Subject, @QueryValue tenantName: String, @Body groupCreator: GroupEntityCreator): Single<HttpResponse<GroupEntity>> {
        return groupService.createGroup(groupCreator.toGroupEntity(tenant = TenantMapKey(tenantName).toUUID()), subject)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Update a Group
     * @param tenantName Name of the tenant
     * @param groupName Name of the group
     * @return The Group
     */
    @Patch("/{groupName}")
    fun update(subject: Subject, @QueryValue tenantName: String, @QueryValue groupName: String, @Body groupModifier: GroupEntityModifier): Single<HttpResponse<GroupEntity>> {
        return groupService.getGroup(GroupMapKey(groupName, tenantName))
                .flatMap { ge ->
                    groupService.updateGroup(groupModifier.toGroupEntity(ge.internalId, ge.name, ge.tenant), subject)
                }.map {
                    HttpResponse.ok(it)
                }
    }

    @Error
    fun argumentError(request: HttpRequest<*>, exception: IllegalArgumentException): HttpResponse<String> {
        if (log.isDebugEnabled) {
            log.debug(exception.message, exception)
        }
        return HttpResponse.badRequest(exception.message)
    }

    @Error
    fun authzError(request: HttpRequest<*>, exception: AuthorizationException): HttpResponse<Unit> {
        log.error(exception.message, exception) //@TODO move to a Authorization Logger
        return HttpResponse.unauthorized()
    }
}