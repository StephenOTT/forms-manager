package formsmanager.tenants.controler

import formsmanager.exception.NotFoundException
import formsmanager.tenants.domain.TenantEntity
import formsmanager.tenants.domain.TenantEntityCreator
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
import java.util.*


@Controller("/tenant")
//@RequiresAuthentication
@RequiresGuest
class TenantController(
        private val tenantService: TenantService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TenantController::class.java)
    }

    /**
     * Get a tenant.
     * @param uuid the Id of the tenant.
     * @return The tenant.
     * @exception NotFoundException Could not find tenant based on Id.
     */
    @Get("/{uuid}")
    fun getTenant(subject: Subject, @QueryValue uuid: UUID): Single<HttpResponse<TenantEntity>> {
        return tenantService.getTenant(uuid, subject)
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
    fun createTenant(subject: Subject, @Body tenant: TenantEntityCreator): Single<HttpResponse<TenantEntity>> {
        return tenantService.createTenant(
                tenant.toTenantEntity(UUID.randomUUID()),
                subject
        ).map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Update a Tenant
     * @param uuid The Id of the tenant to be updated.
     * @param tenant The updated Tenant.
     * @return The Tenant
     */
    @Patch("/{uuid}")
    fun updateTenant(subject: Subject, @QueryValue uuid: UUID, @Body tenant: TenantEntityCreator): Single<HttpResponse<TenantEntity>> {
        return tenantService.updateTenant(tenant.toTenantEntity(uuid), subject)
                .map {
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