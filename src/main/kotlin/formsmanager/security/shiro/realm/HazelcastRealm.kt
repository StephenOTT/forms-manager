package formsmanager.security.shiro.realm

import formsmanager.security.shiro.PasswordService
import formsmanager.security.shiro.principal.PrimaryPrincipal
import formsmanager.users.service.UserService
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SaltedAuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.codec.Base64
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.util.ByteSource
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

/**
 * Realm for username/password login and for Authorization from the User Service.
 */
@Singleton
class HazelcastRealm(
        private val userService: UserService
) : AuthorizingRealm() {

    private val log = LoggerFactory.getLogger(HazelcastRealm::class.java)

    init {
        // Set the Credential matcher
        this.credentialsMatcher = HashedCredentialsMatcher(PasswordService.hashAlgorithmName)
                .apply {
                    hashIterations = PasswordService.hashIterationsCount
                    isStoredCredentialsHexEncoded = false
                }

        this.setAuthenticationTokenClass(UsernamePasswordToken::class.java)
    }

    /**
     * Used for Username/Password Login
     */
    override fun doGetAuthenticationInfo(token: AuthenticationToken): SaltedAuthenticationInfo? {
        if (token is UsernamePasswordToken) {

            //@TODO refactor this with a new UsernamePasswordToken that accepts a Tenant.  Must also refactor the UserDetails class for Micronaut, and the default Micronaut security controller for /login
            val email = token.username.substringAfter(":", "")
            val tenant = UUID.fromString(token.username.substringBefore(":", ""))

            return kotlin.runCatching {
                userService.getUser(email, tenant).map {
                    //@TODO review if the Base64.decode is actually required.  Saw somewhere there is auto decode/detection based on configuration in the realm.

                    //Note: The order of the SimplePrincipalCollection list matters: The first item in the list is considered the "Primary Principal".  See Shiro docs for more info.
                    SimpleAuthenticationInfo(
                            SimplePrincipalCollection(listOf(PrimaryPrincipal(it.id, it.emailInfo.email, it.tenant)), "default"),
                            Base64.decode(it.passwordInfo.passwordHash),
                            ByteSource.Util.bytes(Base64.decode(it.passwordInfo.salt))
                    )
//                    SimpleAuthenticationInfo(
//                            it.emailInfo.email,
//                            Base64.decode(it.passwordInfo.passwordHash),
//                            ByteSource.Util.bytes(Base64.decode(it.passwordInfo.salt)),
//                            "default"
//                    )

                }.subscribeOn(Schedulers.io()).blockingGet()
            }.getOrNull()

        } else {
            throw IllegalStateException("Unsupported AuthenticationToken type.  Only UsernamePasswordToken is supported for this realm.")
        }
    }

    /**
     * General Authorization system
     */
    override fun doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo {
        //@TODO redo this with adding the userId as a typed value into the principals as well as a typed email into the principals

        check(principals.primaryPrincipal is PrimaryPrincipal) {
            "Unsupported Primary Principal found. Only EmailTenantPrincipal is supported as Primary Principal for this realm"
        }
        val primPrincipal: PrimaryPrincipal = (principals.primaryPrincipal as PrimaryPrincipal)

        return userService.getUser(primPrincipal.userId, primPrincipal.tenant).map { ue ->
            val authz = SimpleAuthorizationInfo(ue.rolesInfo.roles.map { it.name }.toSet())

            ue.rolesInfo.roles.forEach {
                authz.addObjectPermissions(it.permissionStringsToWildcardPermissions())
            }
            //@TODO add custom permission adding based on users custom permissions not related to roles

            authz

        }.subscribeOn(Schedulers.io()).blockingGet()
    }
}