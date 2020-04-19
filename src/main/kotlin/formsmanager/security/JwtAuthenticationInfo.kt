package formsmanager.security

import com.nimbusds.jwt.JWT
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection

/**
 * Custom Response for AuthenticationInfo that provides a Principal Collection based on the subject in the JWT.
 * This is used only for JWT Realm Authentication / Shiro Login with a JWT.
 */
data class JwtAuthenticationInfo(val token: JwtToken): AuthenticationInfo {
    override fun getCredentials(): JWT {
        return token.credentials
    }

    override fun getPrincipals(): PrincipalCollection {
        return SimplePrincipalCollection(token.principal, "jwt-default")
    }
}