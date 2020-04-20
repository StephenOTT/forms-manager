package formsmanager.users.controller

import formsmanager.users.domain.CompleteRegistrationRequest
import formsmanager.users.domain.UserRegistration
import formsmanager.users.domain.UserRegistrationResponse
import formsmanager.users.service.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Single
import org.apache.shiro.subject.Subject

@Controller("/users")
class UsersController(
        private val userService: UserService
) {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/register")
    fun register(@Body registration: UserRegistration): Single<HttpResponse<UserRegistrationResponse>> {
        return userService.userExists(registration.email).flatMap { exists ->
            if (exists) {
                Single.error(IllegalArgumentException("Email already exists."))
            } else {
                userService.createUser(registration.email)
            }
        }.map {
            HttpResponse.created(
                    UserRegistrationResponse("Pending email verification")
            )
        }
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/register/complete")
    fun completeRegistration(@Body body: CompleteRegistrationRequest): Single<HttpResponse<UserRegistrationResponse>>{
        return userService.completeRegistration(body.id, body.email, body.emailConfirmToken, body.pwdResetToken, body.cleartextPassword) .map {
            body.destroyPwd()
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