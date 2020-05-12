package formsmanager.core.security.rule

import formsmanager.core.ifDebugEnabled
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * Primary validator code for Shiro Annotation processing.
 * SecurityRules that evaluate Shiro Security Annotations have their checks done in this singleton.
 * Replace this singleton if you wish to provide more detailed checks
 * Checks returning true means success/allow.  Returning False means failure/denied.
 */
@Singleton
class ShiroAnnotationRuleValidator {

    private val log = LoggerFactory.getLogger(ShiroAnnotationRuleValidator::class.java)

    fun checkRequiresAuthentication(subject: Subject): Boolean {
        log.ifDebugEnabled { "checkRequiresAuthentication called." }
        return subject.isAuthenticated
    }

    fun checkRequiresGuest(subject: Subject): Boolean {
        log.ifDebugEnabled { "checkRequiresGuest called." }
        return subject.principal == null
    }

    fun checkRequiresUser(subject: Subject): Boolean {
        log.ifDebugEnabled { "checkRequiresUser called." }
        return subject.isAuthenticated || subject.isRemembered
    }

    fun checkRequiresPermissions(subject: Subject, permissions: List<String>, logicalValue: Logical): Boolean {
        log.ifDebugEnabled { "checkRequiresPermissions called." }
        val permissionsArray = permissions.toTypedArray()
        return when (logicalValue) {
            Logical.AND -> {
                // Returns true if the user has all of the defined roles
                //@TODO review usage of spread operator
                return subject.isPermittedAll(*permissionsArray)
            }
            Logical.OR -> {
                // Returns true if the user has any of the roles
                return subject.isPermitted(*permissionsArray).any { it }
            }
            else -> {
                // If Logical is not AND and not OR
                false
            }
        }
    }

    fun checkRequiresRoles(subject: Subject, roles: List<String>, logicalValue: Logical): Boolean {
        log.ifDebugEnabled { "checkRequiresRoles called." }
        return when (logicalValue) {
            Logical.AND -> {
                // Returns true if the user has all of the defined roles
                return subject.hasAllRoles(roles)
            }
            Logical.OR -> {
                // Returns true if the user has any of the roles
                return subject.hasRoles(roles).any { it }
            }
            else -> {
                // If Logical is not AND and not OR
                false
            }
        }
    }

    /**
     * Is Authenticated or is a Guest/Anonymous.
     * For use with the RequiresAuthenticatedOrGuest annotation
     */
    fun checkRequiresAuthenticatedOrGuest(subject: Subject): Boolean {
        log.ifDebugEnabled { "checkRequiresAuthenticatedOrGuest called." }
        // Is Authenticated or is a Guest/Anonymous.
        return (subject.isAuthenticated || !subject.isAuthenticated) && !subject.isRemembered
    }

}