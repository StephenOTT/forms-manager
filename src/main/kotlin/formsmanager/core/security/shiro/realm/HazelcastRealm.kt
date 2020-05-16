package formsmanager.core.security.shiro.realm

import com.hazelcast.core.HazelcastInstance
import formsmanager.core.security.groups.service.GroupService
import formsmanager.core.security.groups.service.getGroups
import formsmanager.core.security.roles.domain.Role
import formsmanager.core.security.roles.service.RoleService
import formsmanager.core.security.roles.service.getRoles
import formsmanager.core.security.shiro.credentials.PasswordService
import formsmanager.core.security.shiro.principal.PrimaryPrincipal
import formsmanager.tenants.domain.TenantId
import formsmanager.users.service.UserService
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SaltedAuthenticationInfo
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.cache.Cache
import org.apache.shiro.cache.CacheManager
import org.apache.shiro.cache.MapCache
import org.apache.shiro.codec.Base64
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.util.ByteSource
import org.slf4j.LoggerFactory
import javax.inject.Singleton

data class LoginToken(
        private val username: String,
        private val password: CharArray,
        private val tenant: TenantId
): AuthenticationToken {

    data class UsernameTenant(
            val username: String,
            val tenant: TenantId
    )

    private val usernameTenant: UsernameTenant = UsernameTenant(username, tenant)

    override fun getCredentials(): CharArray {
        return password
    }

    override fun getPrincipal(): UsernameTenant {
        return usernameTenant
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginToken

        if (username != other.username) return false
        if (!password.contentEquals(other.password)) return false
        if (tenant != other.tenant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.contentHashCode()
        result = 31 * result + tenant.hashCode()
        return result
    }


}


/**
 * Realm for username/password login and for Authorization from the User Service.
 */
@Singleton
class HazelcastRealm(
        private val userService: UserService,
        cacheManager: HazelcastRealmCacheManager,
        private val groupService: GroupService,
        private val roleService: RoleService
) : AuthorizingRealm(cacheManager) {

    private val log = LoggerFactory.getLogger(HazelcastRealm::class.java)

    init {

        // Set the Credential matcher
        this.credentialsMatcher = HashedCredentialsMatcher(PasswordService.hashAlgorithmName)
                .apply {
                    hashIterations = PasswordService.hashIterationsCount
                    isStoredCredentialsHexEncoded = false
                }

        this.setAuthenticationTokenClass(LoginToken::class.java)


    }

    /**
     * Used for Username/Password Login
     */
    override fun doGetAuthenticationInfo(token: AuthenticationToken): SaltedAuthenticationInfo? {
        if (token is LoginToken) {

            //@TODO refactor this with a new UsernamePasswordToken that accepts a Tenant.  Must also refactor the UserDetails class for Micronaut, and the default Micronaut security controller for /login
//            val email = token.username.substringAfter(":", "")
//            val tenantName = token.username.substringBefore(":", "")

            return kotlin.runCatching {
                userService.getByUsername(token.principal.username, token.principal.tenant).map {
                    //@TODO review if the Base64.decode is actually required.  Saw somewhere there is auto decode/detection based on configuration in the realm.

                    //Note: The order of the SimplePrincipalCollection list matters: The first item in the list is considered the "Primary Principal".  See Shiro docs for more info.
                    SimpleAuthenticationInfo(
                            SimplePrincipalCollection(listOf(PrimaryPrincipal(it.id, it.tenant)), "default"),
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
            "Unsupported Primary Principal found. Only PrimaryPrincipal.class is supported as Primary Principal for this realm"
        }
        val primPrincipal: PrimaryPrincipal = (principals.primaryPrincipal as PrimaryPrincipal)

        return userService.get(primPrincipal.userId).map { ue ->

            val roles: Set<Role> = ue.groupInfo.groups.getGroups(groupService).subscribeOn(Schedulers.io()).blockingGet().flatMap {
                it.roles.getRoles(roleService).subscribeOn(Schedulers.io()).blockingGet()
            }.toSet()


            val authz = SimpleAuthorizationInfo()

            roles.forEach {
                authz.addRole(it.name)
                authz.addStringPermissions(it.permissions)
            }
            //@TODO add custom permission adding based on users custom permissions not related to roles

            authz

        }.subscribeOn(Schedulers.io()).blockingGet()
    }
}

@Singleton
class HazelcastRealmCacheManager(
        private val hazelcastInstance: HazelcastInstance
): CacheManager {

    //@TODO add near cache config on maps
    private val cacheMapPrefix = "shiro_cache__"

    override fun <K : Any, V : Any> getCache(cacheName: String): Cache<K, V> {
        val name = cacheMapPrefix+cacheName
        val backingMap = hazelcastInstance.getMap<K,V>(name)
        return MapCache(name, backingMap) //@TODO review usage of MapCache
    }

}
