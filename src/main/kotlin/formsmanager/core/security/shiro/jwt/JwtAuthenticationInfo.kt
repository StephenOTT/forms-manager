package formsmanager.core.security.shiro.jwt

import formsmanager.core.security.shiro.principal.PrimaryPrincipal
import formsmanager.users.UserMapKey
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection

/**
 * Custom Response for AuthenticationInfo that provides a Principal Collection based on the subject in the JWT.
 * This is used only for JWT Realm Authentication / Shiro Login with a JWT.
 */
data class JwtAuthenticationInfo(val email: String,
                                 val tenantName: String,
                                 val jwtToken: JwtToken): AuthenticationInfo {
    /**
     * Not used
     */
    override fun getCredentials() {
    }

    override fun getPrincipals(): PrincipalCollection {
        //@TODO review to add jwtToken object into the Principals collection
        return SimplePrincipalCollection(PrimaryPrincipal(UserMapKey(email, tenantName).toUUID()), "jwt-default")
    }
}