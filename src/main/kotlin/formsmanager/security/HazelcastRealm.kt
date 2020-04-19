package formsmanager.security

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
import org.apache.shiro.util.ByteSource
import org.slf4j.LoggerFactory
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
            return kotlin.runCatching {
                userService.getUser(token.username).map {
                    SimpleAuthenticationInfo(
                            it.emailInfo.email,
                            Base64.decode(it.passwordInfo.passwordHash),
                            ByteSource.Util.bytes(Base64.decode(it.passwordInfo.salt)),
                            "default"
                    )

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
        return userService.getUser(principals.primaryPrincipal.toString()).map { ue ->
            val authz = SimpleAuthorizationInfo(ue.rolesInfo.roles.map { it.name }.toSet())

            ue.rolesInfo.roles.forEach {
                authz.addObjectPermissions(it.permissionStringsToWildcardPermissions())
            }

            authz
            //@TODO add custom permission adding based on users custom permissions

        }.subscribeOn(Schedulers.io()).blockingGet()
    }
}