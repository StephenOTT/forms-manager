package formsmanager.core.security.shiro.jwt

import org.apache.shiro.authc.AuthenticationToken

/**
 * Shiro AuthenticationToken for JWT Tokens
 * "tenantName:email"
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