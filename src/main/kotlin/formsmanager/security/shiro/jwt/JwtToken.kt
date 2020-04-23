package formsmanager.security.shiro.jwt

import com.nimbusds.jwt.JWT
import org.apache.shiro.authc.AuthenticationToken

/**
 * Shiro AuthenticationToken for JWT Tokens
 */
data class JwtToken(private val subjectPrincipal: String) : AuthenticationToken {

    /**
     * Not used
     */
    override fun getCredentials() {
    }

    /**
     * Returns subject from JWT
     */
    override fun getPrincipal(): String {
        return subjectPrincipal
    }
}