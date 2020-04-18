package formsmanager.security

import formsmanager.ifDebugEnabled
import formsmanager.users.service.UserService
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.runtime.http.scope.RequestScope
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authc.*
import org.apache.shiro.authc.credential.DefaultPasswordService
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.codec.Base64
import org.apache.shiro.crypto.hash.DefaultHashService
import org.apache.shiro.crypto.hash.Hash
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.env.DefaultEnvironment
import org.apache.shiro.env.Environment
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator
import org.apache.shiro.mgt.DefaultSubjectDAO
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.realm.Realm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ByteSource
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Factory for the generation of Shiro singletons: Realm, Subject (Request Scope), Security Manager, etc.
 */
@Factory
class ShrioFactory {

    /**
     * Default Shiro Environment
     * Injects all Realm Singletons into the Security Manager.
     * Uses DefaultSecurityManager (with SessionStorage disabled).
     * Uses DefaultEnvironment
     * Uses default authentication strategy for multiple realms: First Successful Realm Authentication will support the login.
     */
    @Singleton
    @Primary
    @Named("default")
    @Context
    fun environment(realms: List<Realm>): Environment {
        val manager = DefaultSecurityManager(realms)

        // Disable sessions for subjects:
        // See notes in https://shiro.apache.org/session-management.html#SessionManagement-DisablingSubjectStateSessionStorage
        manager.subjectDAO = DefaultSubjectDAO()
                .apply {
                    sessionStorageEvaluator = DefaultSessionStorageEvaluator()
                            .apply {
                                isSessionStorageEnabled = false
                            }
                }

        //@TODO review if this can be changed to a just custom env, or if the DefaultEnv really provides something "needed"
        val env = DefaultEnvironment()
        env.securityManager = manager

        return env
    }

    /**
     * Shortcut helper bean to directly get the SecurityManager from the primary Environment bean.
     */
    @Singleton
    @Primary
    @Named("default")
    fun securityManager(environment: Environment): SecurityManager {
        return environment.securityManager
    }

    /**
     * The Shiro subject of the HTTP Request.
     * The Subject is associated to the primary SecurityManager bean
     */
    @RequestScope
    @Bean(preDestroy = "logout") // extra cleanup
    @Primary
    @Named("http-request")
    fun subject(securityManager: SecurityManager): Subject {
        return Subject.Builder(securityManager).buildSubject()
    }

}


@Singleton
class HazelcastRealm(
        private val userService: UserService
) : AuthorizingRealm() {

    companion object {
        //@TODO setup config
        //@TODO add optional ini setup?

        const val hashIterationsCount: Int = 500000 //500,000
        const val hashAlgorithmName: String = Sha512Hash.ALGORITHM_NAME

        private val log = LoggerFactory.getLogger(HazelcastRealm::class.java)
    }

    private val passwordService = DefaultPasswordService()

    init {
        // Set the Hash Algorithm
        (passwordService.hashService as DefaultHashService).hashAlgorithmName = hashAlgorithmName

        // Set the Credential matcher
        this.credentialsMatcher = HashedCredentialsMatcher(hashAlgorithmName)
                .apply {
                    hashIterations = hashIterationsCount
                    isStoredCredentialsHexEncoded = false
                }

        this.setAuthenticationTokenClass(UsernamePasswordToken::class.java)
    }

    /**
     * Hashes cleartext password.
     * Subscribes on RxJava Computation Scheduler
     */
    fun hashPassword(cleartextPassword: CharArray): Single<Hash> {
        return Single.fromCallable {
            log.ifDebugEnabled { "Generating Hashed Password." }
            // Bytesource is already done by the hashPassword...
            passwordService.hashPassword(cleartextPassword)
        }.subscribeOn(Schedulers.computation())
    }

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
            throw IllegalArgumentException("Unsupported AuthenticationToken type.  Only UsernamePasswordToken is supported for this realm.")
        }
    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo {
        //@TODO redo this with adding the userId as a typed value into the principals as well as a typed email into the principals
        return userService.getUser(principals.primaryPrincipal.toString() ).map { ue ->
            val authz = SimpleAuthorizationInfo(ue.rolesInfo.roles.map { it.name }.toSet())

            ue.rolesInfo.roles.forEach {
                authz.addObjectPermissions(it.permissionStringsToWildcardPermissions())
            }

            authz
            //@TODO add custom permission adding based on users custom permissions

        }.subscribeOn(Schedulers.io()).blockingGet()
    }
}

