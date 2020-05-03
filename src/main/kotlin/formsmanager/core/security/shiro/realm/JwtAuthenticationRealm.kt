package formsmanager.core.security.shiro.realm

import formsmanager.core.security.shiro.jwt.JwtAuthenticationInfo
import formsmanager.core.security.shiro.jwt.JwtToken
import formsmanager.users.UserMapKey
import formsmanager.users.service.UserService
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.realm.AuthenticatingRealm
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

/**
 * Realm used for JWT Authentication / Creating a subject based on a provided JWT.
 * Does not provide Authorization capabilities (does not use any roles or permissions within the JWT)
 */
@Singleton
class JwtAuthenticationRealm(
        private val userService: UserService
) : AuthenticatingRealm() {

    private val log = LoggerFactory.getLogger(HazelcastRealm::class.java)

    init {
        this.setAuthenticationTokenClass(JwtToken::class.java)
    }

    /**
     * Used for JWT Authentication: This does not validate the JWT; it only produces the Principals/Auth Info for the Provided JWT.
     * JWT validation is the role of the calling framework (in this case Micronaut Security)
     */
    override fun doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo? {
        if (token is JwtToken) {

            //@TODO refactor to update the Micronaut JWT to include a tenant claim.
            val email: String = token.principal.substringAfter(":", "")
            val tenantName: String = token.principal.substringBefore(":", "")

            //@TODO review for better handling of a bad login.  Currently not working correctly when principal name cannot be found
            return kotlin.runCatching {
                userService.getUser(UserMapKey(email, tenantName)).map {
                    if (it.accountActive()){
                        JwtAuthenticationInfo(email, tenantName, token)
                    } else {
                        throw IllegalArgumentException("JWT cannot be accepted: Account is not active.")
                    }
                }.subscribeOn(Schedulers.io()).blockingGet()
            }.onFailure {
                log.error("JWT Login error: ${it.message}", it)
            }.getOrNull()

        } else {
            throw IllegalStateException("Unsupported AuthenticationToken type.  Only JwtToken is supported for this realm.")
        }
    }

}