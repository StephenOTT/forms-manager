package formsmanager.security

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import formsmanager.ifDebugEnabled
import io.micronaut.context.annotation.Replaces
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.generator.claims.JwtClaimsSetAdapter
import io.micronaut.security.token.jwt.validator.AuthenticationJWTClaimsSetAdapter
import io.micronaut.security.token.jwt.validator.DefaultJwtAuthenticationFactory
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.support.DefaultSubjectContext
import org.slf4j.LoggerFactory
import java.text.ParseException
import java.util.*
import javax.inject.Singleton


class ShiroAuthenicationJwtClaimsSetAdapter(claims: JWTClaimsSet) : AuthenticationJWTClaimsSetAdapter(claims) {

    lateinit var shrioSubject: Subject

}

/**
 * Only gets called if JWT was validated
 * https://github.com/micronaut-projects/micronaut-security/blob/1.3.x/security-jwt/src/main/java/io/micronaut/security/token/jwt/validator/JwtTokenValidator.java#L168
 *
 * Additional code comments have been added for future understanding
 */
@Singleton
@Replaces(DefaultJwtAuthenticationFactory::class)
class ShiroJwtAuthenticationFactory(
        private val securityManager: SecurityManager
) : DefaultJwtAuthenticationFactory() {

    private val logger = LoggerFactory.getLogger(ShiroJwtAuthenticationFactory::class.java)

    override fun createAuthentication(token: JWT): Optional<Authentication> {
        // Copy of the super's function with a modification of the adapter class:
        val response = runCatching<Optional<Authentication>> {
            val claimSet = token.jwtClaimsSet
            // Modified with a custom adapter to add additional Shiro Subject storage as the JWT ClaimSets are immutable
            Optional.of(ShiroAuthenicationJwtClaimsSetAdapter(claimSet))
        }.onFailure {
            if (logger.isErrorEnabled) {
                logger.error("ParseException creating authentication", it)
            }
        }.getOrDefault(Optional.empty())


        // Add some additional logic for Shiro Login based on the success of the Micronaut Security JWT Auth chain:
        //Response is present if JWT was successfully accepted / was valid
        return if (response.isPresent) {
            // Use the Authentication object's attributes map to store the reference to the Shiro Subject:
            // The Attributes were loaded from the JWT Claims.

            val subject = securityManager.createSubject(DefaultSubjectContext()) // @TODO Review for customizations of the default subject context (such as host)

            // Try to login to Shiro using the JWT
            val loginAttempt = kotlin.runCatching {
                // Will throw a error if login failed.  if login was successful then void response.
                subject.login(JwtToken(token)) // @TODO Consider sending the Authentication Object in rather than raw token
            }

            //@TODO refactor
            if (loginAttempt.isSuccess) {
                // Sets the attribute in the Authentication response.  This can be used by the Binder
                logger.ifDebugEnabled { "Shiro Subject was authenticated as ${subject.principal}" }

                (response.get() as ShiroAuthenicationJwtClaimsSetAdapter).shrioSubject = subject

                // Return the original response object but now with the shiro subject in the attributes
                response

            } else {
                // If the login failed, then rethrow the exception
                throw loginAttempt.exceptionOrNull()!!
            }
        } else {
            // returns the empty response: occurs when JWT was not valid
            response
        }
    }
}