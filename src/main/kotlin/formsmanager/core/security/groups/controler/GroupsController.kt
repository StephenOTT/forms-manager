package formsmanager.core.security.groups.controler

import formsmanager.core.exception.NotFoundException
import formsmanager.core.hazelcast.query.sql.filterable.FilterError
import formsmanager.core.hazelcast.query.sql.filterable.FilterException
import formsmanager.core.hazelcast.query.sql.filterable.Filterable
import formsmanager.core.hazelcast.query.sql.validator.SqlPredicateRules.SqlPredicates.*
import formsmanager.core.hazelcast.query.sql.binder.FilterableControl
import formsmanager.core.security.groups.domain.Group
import formsmanager.core.security.groups.domain.GroupCreator
import formsmanager.core.security.groups.domain.GroupModifier
import formsmanager.core.security.groups.service.GroupService
import formsmanager.tenants.domain.TenantId
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.subject.Subject
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@RequiresAuthentication
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
    fun get(subject: Subject, @QueryValue tenantName: TenantId, @QueryValue groupName: String, filter: Filterable?): Single<HttpResponse<Group>> {
        return groupService.getByName(groupName, tenantName, subject)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Create a Group.
     * @param tenantName The Tenant to be created.
     * @return the created Tenant
     */
    @Post("/")
    fun create(subject: Subject, @QueryValue tenantName: TenantId, @Body groupCreator: GroupCreator): Single<HttpResponse<Group>> {
        return groupService.create(groupCreator.toGroupEntity(tenant = tenantName), subject)
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
    fun update(subject: Subject, @QueryValue tenantName: TenantId, @QueryValue groupName: String, @Body groupModifier: GroupModifier): Single<HttpResponse<Group>> {
        return groupService.getByName(groupName, tenantName)
                .flatMap { ge ->
                    groupService.update(groupModifier.toGroupEntity(ge.id, ge.tenant), subject)
                }.map {
                    HttpResponse.ok(it)
                }
    }

    @Get("/")
    fun search(subject: Subject,
               @QueryValue tenantName: String,
               @FilterableControl(
                       allowProperties = ["name"],
                       prohibitTypes = [REGEX, BETWEEN, LIKE, ILIKE])
               filter: Filterable,
               pageable: Pageable): Single<HttpResponse<List<Group>>> {

        return groupService.search(filter.toPredicate(), pageable, subject)
                .toList().map {
                    HttpResponse.ok(it)
                }
    }

    @Error
    fun filterError(request: HttpRequest<*>, exception: FilterException): HttpResponse<FilterError> {
        if (log.isDebugEnabled) {
            log.debug(exception.message, exception)
        }
        return HttpResponse.badRequest(exception.toFilterError())
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