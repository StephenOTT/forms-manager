package formsmanager.tenants.controler

import formsmanager.core.exception.NotFoundException
import formsmanager.tenants.domain.Tenant
import formsmanager.tenants.domain.TenantCreator
import formsmanager.tenants.domain.TenantModifier
import formsmanager.tenants.service.TenantService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.subject.Subject
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@Controller("/tenant")
@RequiresAuthentication
//@RequiresGuest
class TenantController(
        private val tenantService: TenantService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TenantController::class.java)
    }

    /**
     * Get a tenant.
     * @param tenantName the name of the tenant.
     * @return The tenant.
     * @exception NotFoundException Could not find tenant based on Id.
     */
    @Get("/{tenantName}")
    fun getTenant(subject: Subject, @QueryValue tenantName: String): Single<HttpResponse<Tenant>> {
        return tenantService.getByName(tenantName, subject)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Create a Tenant.
     * @param tenant The Tenant to be created.
     * @return the created Tenant
     */
    @Post("/")
    fun createTenant(subject: Subject, @Body tenant: TenantCreator): Single<HttpResponse<Tenant>> {
        return tenantService.create(tenant.toTenant())//@TODO !!** RE-ADD The Subject
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Update a Tenant
     * @param uuid The Id of the tenant to be updated.
     * @param tenant The updated Tenant.
     * @return The Tenant
     */
    @Patch("/{tenantName}")
    fun updateTenant(subject: Subject, @QueryValue tenantName: String, @Body tenant: TenantModifier): Single<HttpResponse<Tenant>> {
        return tenantService.getByName(tenantName)
                .flatMap { te ->
                    tenantService.update(tenant.toTenant(te.id))
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