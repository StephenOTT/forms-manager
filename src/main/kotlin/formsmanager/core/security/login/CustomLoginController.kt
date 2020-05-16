package formsmanager.core.security.login

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import formsmanager.core.typeconverter.TenantNameToTenantIdDeserializer
import formsmanager.tenants.domain.TenantId
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.*
import io.micronaut.security.endpoints.LoginController
import io.micronaut.security.endpoints.LoginControllerConfigurationProperties
import io.micronaut.security.event.LoginFailedEvent
import io.micronaut.security.event.LoginSuccessfulEvent
import io.micronaut.security.handlers.LoginHandler
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import io.reactivex.Flowable
import io.reactivex.Single
import net.minidev.json.annotate.JsonIgnore
import java.io.Serializable
import javax.validation.Valid

/**
 * Replaces LoginController to provide customizations
 * @param authenticator [Authenticator] collaborator
 * @param loginHandler A collaborator which helps to build HTTP response depending on success or failure.
 * @param eventPublisher The application event publisher
 **/
@Replaces(LoginController::class)
@Requires(property = LoginControllerConfigurationProperties.PREFIX + ".enabled", value = StringUtils.TRUE)
@Controller("\${" + LoginControllerConfigurationProperties.PREFIX + ".path:/login}")
@Secured(SecurityRule.IS_ANONYMOUS)
@Validated
class CustomLoginController(
        protected val authenticator: Authenticator,
        protected val loginHandler: LoginHandler,
        protected val eventPublisher: ApplicationEventPublisher) {

    /**
     * @param credentials An instance of [UsernamePasswordCredentials] in the body payload
     * @param request The [HttpRequest] being executed
     * @return An AccessRefreshToken encapsulated in the HttpResponse or a failure indicated by the HTTP status
     */
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON)
    @Post
    fun login(@Body credentials: @Valid TenantBasedCredentials, request: HttpRequest<*>): Single<HttpResponse<*>> {
        val authenticationResponseFlowable = Flowable.fromPublisher(authenticator.authenticate(request, credentials))

        return authenticationResponseFlowable.map { authenticationResponse: AuthenticationResponse ->
            if (authenticationResponse.isAuthenticated) {
                val userDetails = authenticationResponse as UserDetails
                eventPublisher.publishEvent(LoginSuccessfulEvent(userDetails))
                loginHandler.loginSuccess(userDetails, request)

            } else {
                eventPublisher.publishEvent(LoginFailedEvent(authenticationResponse))
                loginHandler.loginFailed(authenticationResponse)
            }

        }.first(HttpResponse.status<Unit>(HttpStatus.UNAUTHORIZED))
    }

    @Error
    fun formValidationError(request: HttpRequest<*>, exception: Exception): HttpResponse<String> {
        //@TODO refactor this with proper error message.
        exception.printStackTrace()
        return HttpResponse.serverError(exception.message)
    }
}


/**
 * Login form for the /login endpoints.
 * Used as a replacement for the default MN credentials.
 * Mainly used to add the tenant parameter.
 */
@Introspected
data class TenantBasedCredentials(
        val username: String,
        val password: CharArray,
        @field:JsonDeserialize(using = TenantNameToTenantIdDeserializer::class)
        val tenant: TenantId
) : Serializable, AuthenticationRequest<LoginIdentity, CharArray> {

    @JsonIgnore
    override fun getIdentity(): LoginIdentity {
        return LoginIdentity(username, tenant)
    }

    @JsonIgnore
    override fun getSecret(): CharArray {
        return password
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TenantBasedCredentials

        if (username != other.username) return false
        if (!password.contentEquals(other.password)) return false
        if (tenant != other.tenant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.contentHashCode()
        result = 31 * result + tenant.hashCode()
        return result
    }


}

/**
 * Wrapper object for username/tenant
 */
@Introspected
data class LoginIdentity(
        val username: String,
        val tenant: TenantId
): Serializable
