package formsmanager.security.shiro

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import org.apache.shiro.env.DefaultEnvironment
import org.apache.shiro.env.Environment
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator
import org.apache.shiro.mgt.DefaultSubjectDAO
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.Realm
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Factory for the generation of Shiro singletons: Realm, Subject (Request Scope), Security Manager, etc.
 */
@Factory
class ShiroFactory {

    private val log = LoggerFactory.getLogger(ShiroFactory::class.java)

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

}