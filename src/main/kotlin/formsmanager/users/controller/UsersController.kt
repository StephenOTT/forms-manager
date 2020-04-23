package formsmanager.users.controller

import formsmanager.users.domain.CompleteRegistrationRequest
import formsmanager.users.domain.UserRegistration
import formsmanager.users.domain.UserRegistrationResponse
import formsmanager.users.service.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import java.util.*

@Controller("/users/{tenant}")
@RequiresGuest
class UsersController(
        private val userService: UserService
) {

    @Post("/register")
    fun register(@QueryValue tenant: UUID, @Body registration: UserRegistration): Single<HttpResponse<UserRegistrationResponse>> {
        return userService.userExists(registration.email, tenant).flatMap { exists ->
            if (exists) {
                Single.error(IllegalArgumentException("Email already exists."))
            } else {
                //@TODO create a Tenant Map Registery to validate that the Tenant Exists.
                userService.createUser(registration.email, tenant)
            }
        }.map {
            HttpResponse.created(
                    UserRegistrationResponse("Pending email verification")
            )
        }
    }

    @Post("/register/complete")
    fun completeRegistration(@Body body: CompleteRegistrationRequest): Single<HttpResponse<UserRegistrationResponse>>{
        return userService.completeRegistration(body.id, body.email, body.emailConfirmToken, body.pwdResetToken, body.cleartextPassword) .map {
            body.destroyPwd() //@TODO review if this is needed
            HttpResponse.created(UserRegistrationResponse("completed"))
        }
    }

    @Error
    fun formValidationError(request: HttpRequest<*>, exception: IllegalArgumentException): HttpResponse<UserRegistrationResponse> {
        return HttpResponse.created(
                UserRegistrationResponse("Pending email verification")
        )
    }
}