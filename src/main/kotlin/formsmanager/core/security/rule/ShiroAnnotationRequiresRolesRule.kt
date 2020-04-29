package formsmanager.core.security.rule

import formsmanager.ifDebugEnabled
import formsmanager.core.security.ShiroMicronautSecurityService
import io.micronaut.http.HttpRequest
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.authz.annotation.RequiresRoles
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class ShiroAnnotationRequiresRolesRule(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService,
        private val validator: ShiroAnnotationRuleValidator
) : SecurityRule {

    private val log = LoggerFactory.getLogger(ShiroAnnotationRequiresRolesRule::class.java)

    companion object {
        val ORDER = SecuredAnnotationRule.ORDER - 100
    }

    override fun getOrder(): Int {
        return ORDER
    }

    override fun check(request: HttpRequest<Any>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        return if (routeMatch is MethodBasedRouteMatch<*, *> && routeMatch.hasAnnotation(RequiresRoles::class.java)) {
            log.ifDebugEnabled { "Shrio RequiresRoles annotation was found on route ${routeMatch.name}" }

            val value: Optional<Array<String>> = routeMatch.getValue(RequiresRoles::class.java, Array<String>::class.java)
            val logicalValue: Logical = routeMatch.getValue(RequiresRoles::class.java, "logical", Logical::class.java).orElse(Logical.AND)

            return if (value.isPresent) {
                val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)

                val rolesInAnnList = value.get().asList()

                return if (validator.checkRequiresRoles(subject, rolesInAnnList, logicalValue)) {
                    log.ifDebugEnabled { "Allowed: (route: ${routeMatch.name}) Shiro Authenticated Subject ${subject.principal} has the required roles: $rolesInAnnList" }
                    SecurityRuleResult.ALLOWED
                } else {
                    // Subject does not have the required roles
                    log.ifDebugEnabled { "Rejected: (route: ${routeMatch.name}) Shiro Authenticated Subject ${subject.principal} does not have the required roles: $rolesInAnnList" }
                    SecurityRuleResult.REJECTED
                }
            } else {
                // If Value is null
                log.error("Rejected: Value property was not present in Shiro RequiresRoles annotation on route ${routeMatch.name}")
                SecurityRuleResult.REJECTED
            }
        } else {
            SecurityRuleResult.UNKNOWN
        }
    }
}