package formsmanager.security

import com.nimbusds.jwt.JWT
import org.apache.shiro.authc.AuthenticationToken

/**
 * Shiro AuthenticationToken for JWT Tokens
 */
data class JwtToken(val jwt: JWT) : AuthenticationToken {

    /**
     * Returns the JWT object
     */
    override fun getCredentials(): JWT {
        return jwt
    }

    /**
     * Returns subject from JWT
     */
    override fun getPrincipal(): String {
        return jwt.jwtClaimsSet.subject
    }
}