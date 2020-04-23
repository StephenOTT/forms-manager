package formsmanager.security.jwt

import formsmanager.ifDebugEnabled
import formsmanager.security.ShiroMicronautSecurityService
import formsmanager.security.shiro.jwt.JwtToken
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.TokenAuthenticationFetcher
import io.micronaut.security.token.jwt.validator.JwtTokenValidator
import io.micronaut.security.token.reader.TokenResolver
import io.micronaut.security.token.validator.TokenValidator
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * A replacement for JwtTokenValidator.  Overrides the fetchAuthentication method.
 * The override calls the super, but then if the Publisher returns a object (not empty), then
 * Will attempt to login with Shiro.  Will throw a Auth error if the login fails.
 * JWT Shiro Login is currently just the username.  Login is validated by Micronaut Security JWT validation.
 * Shrio "trusts" the validation of the Token, and accepts the subject of the token as the Shiro Authenticated User/Account/Principal.
 */
@Singleton
@Replaces(JwtTokenValidator::class)
class ShiroTokenAuthenticationFetcher(
        tokenValidators: Collection<TokenValidator>,
        tokenResolver: TokenResolver,
        eventPublisher: ApplicationEventPublisher,
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService
): TokenAuthenticationFetcher(tokenValidators, tokenResolver, eventPublisher){

    private val log = LoggerFactory.getLogger(ShiroTokenAuthenticationFetcher::class.java)

    override fun fetchAuthentication(request: HttpRequest<*>): Publisher<Authentication> {
        val auth = super.fetchAuthentication(request)
        // If the super returns a Authentication object then:
        return Flowable.fromPublisher(auth).map {
            log.ifDebugEnabled { "JWT Successfully validated: Attempting to setup Shiro Subject" }

            val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)

            kotlin.runCatching {
                subject.login(JwtToken(it.name))
            }.onFailure {
                log.ifDebugEnabled { "Login failed: ${it::class.qualifiedName} with message: ${it.message}" }
                throw it //@TODO add better error handler

            }.onSuccess {
                log.ifDebugEnabled { "Login Success as ${subject.principal}" }
            }
            // Return the original Authentication object
            it
        }
    }
}