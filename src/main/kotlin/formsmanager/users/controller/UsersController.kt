package formsmanager.users.controller

import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.service.TenantService
import formsmanager.users.domain.*
import formsmanager.users.service.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.subject.Subject
import java.util.*

@Controller("/users/{tenantName}")
@RequiresGuest
class UsersController(
        private val userService: UserService,
        private val tenantService: TenantService
) {

    @Post("/register")
    @RequiresGuest
    fun register(subject: Subject, @QueryValue tenantName: TenantId, @Body registration: UserRegistration): Single<HttpResponse<UserRegistrationResponse>> {
        return Single.fromCallable {
            User.newUser(registration.email, tenantName, setOf())
        }.flatMap {
            userService.create(it)
        }.map {
            HttpResponse.created(
                    UserRegistrationResponse("Pending email verification")
            )
        }
    }

    @Post("/register/complete")
    @RequiresGuest
    fun completeRegistration(@QueryValue tenantName: TenantId, @Body body: CompleteRegistrationRequest): Single<HttpResponse<UserRegistrationResponse>> {
        //@TODO add tenant logic to use the tenant to pass into the completion check.
        return userService.completeRegistration(
                UserId(UUID.fromString(body.userId)),
                body.emailConfirmToken,
                body.pwdResetToken,
                body.cleartextPassword
        ).map {
            HttpResponse.created(UserRegistrationResponse("completed"))
        }
    }

    @Error
    fun formValidationError(request: HttpRequest<*>, exception: IllegalArgumentException): HttpResponse<UserRegistrationResponse> {
        //@TODO refactor this with proper error message.
        return HttpResponse.created(
                UserRegistrationResponse("Pending email verification")
        )
    }
}