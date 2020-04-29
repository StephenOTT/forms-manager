package formsmanager.users.controller

import formsmanager.tenants.service.TenantService
import formsmanager.users.UserMapKey
import formsmanager.users.domain.CompleteRegistrationRequest
import formsmanager.users.domain.UserRegistration
import formsmanager.users.domain.UserRegistrationResponse
import formsmanager.users.service.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.subject.Subject
import java.util.*

@Controller("/users/{tenant}")
class UsersController(
        private val userService: UserService,
        private val tenantService: TenantService
) {

    @Post("/register")
    @RequiresGuest
    fun register(subject: Subject, @QueryValue tenant: UUID, @Body registration: UserRegistration): Single<HttpResponse<UserRegistrationResponse>> {
        return userService.createUser(registration.email, tenant, subject)
                .map {
                    HttpResponse.created(
                            UserRegistrationResponse("Pending email verification")
                    )
                }
    }

    @Post("/register/complete")
    @RequiresGuest
    fun completeRegistration(@QueryValue tenant: UUID, @Body body: CompleteRegistrationRequest): Single<HttpResponse<UserRegistrationResponse>> {

        val mapKey = UserMapKey(body.email, tenant)

        return userService.completeRegistration(
                mapKey,
                body.emailConfirmToken,
                body.pwdResetToken,
                body.cleartextPassword

        ).map {
            body.destroyPwd() //@TODO review if this is needed
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