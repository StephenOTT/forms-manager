package formsmanager.security

import formsmanager.users.service.UserService
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.realm.AuthenticatingRealm
import org.slf4j.LoggerFactory
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

            return kotlin.runCatching {
                userService.getUser(token.principal).map {
                    if (it.accountActive()){
                        JwtAuthenticationInfo(token)
                    } else {
                        throw IllegalArgumentException("JWT cannot be accepted: Account is not active.")
                    }
                }.subscribeOn(Schedulers.io()).blockingGet()
            }.getOrNull()

        } else {
            throw IllegalStateException("Unsupported AuthenticationToken type.  Only JwtToken is supported for this realm.")
        }
    }

}