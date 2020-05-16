package formsmanager.core.security.roles.controler

import formsmanager.core.exception.NotFoundException
import formsmanager.core.security.roles.domain.Role
import formsmanager.core.security.roles.domain.RoleCreator
import formsmanager.core.security.roles.domain.RoleModifier
import formsmanager.core.security.roles.service.RoleService
import formsmanager.tenants.domain.TenantId
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
//@RequiresGuest
@Controller("security/{tenantName}/roles")
class RolesController(
        private val roleService: RoleService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(RolesController::class.java)
    }

    /**
     * Get a Role.
     * @param tenantName the name of the tenant.
     * @param roleName the name of the role
     * @return The Role.
     * @exception NotFoundException Could not find role based on the role name.
     */
    @Get("/{roleName}")
    fun get(subject: Subject, @QueryValue tenantName: TenantId, @QueryValue roleName: String): Single<HttpResponse<Role>> {
        return roleService.getByName(roleName, tenantName, subject)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Create a Role.
     * @param tenantName The Tenant to be created.
     * @return the created Tenant
     */
    @Post("/")
    fun create(subject: Subject, @QueryValue tenantName: TenantId, @Body roleCreator: RoleCreator): Single<HttpResponse<Role>> {
        return roleService.create(roleCreator.toRole(tenant = tenantName))
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Update a Role
     * @param tenantName Name of the tenant
     * @param roleName Name of the role
     * @return The Role
     */
    @Patch("/{roleName}")
    fun update(subject: Subject, @QueryValue tenantName: TenantId, @QueryValue roleName: String, @Body roleModifier: RoleModifier): Single<HttpResponse<Role>> {
        return roleService.getByName(roleName, tenantName, subject)
                .flatMap { re ->
                    roleService.update(roleModifier.toRole(re.id, re.tenant), subject)
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