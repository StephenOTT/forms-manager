package formsmanager.core.security.rule

import formsmanager.core.ifDebugEnabled
import formsmanager.core.security.ShiroMicronautSecurityService
import io.micronaut.http.HttpRequest
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import org.apache.shiro.authz.annotation.RequiresUser
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ShiroAnnotationRequiresUserRule(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService,
        private val validator: ShiroAnnotationRuleValidator
) : SecurityRule {

    private val log = LoggerFactory.getLogger(ShiroAnnotationRequiresUserRule::class.java)

    companion object {
        val ORDER = SecuredAnnotationRule.ORDER - 100
    }

    override fun getOrder(): Int {
        return ORDER
    }

    override fun check(request: HttpRequest<Any>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        return if (routeMatch is MethodBasedRouteMatch<*, *> && routeMatch.hasAnnotation(RequiresUser::class.java)) {
            log.ifDebugEnabled { "Shrio RequiresUser annotation was found on route ${routeMatch.name}" }

            val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)
            return if (validator.checkRequiresUser(subject)) {
                log.ifDebugEnabled { "Allowed: (route: ${routeMatch.name}) Shiro Subject ${subject.principal} is Authenticated(${subject.isAuthenticated}) or is Remembered(${subject.isRemembered})" }
                SecurityRuleResult.ALLOWED

            } else {
                log.ifDebugEnabled { "Rejected: (route: ${routeMatch.name}) Shiro Subject must be a User: is-Authenticated or is-Remembered." }
                SecurityRuleResult.REJECTED
            }
        } else {
            SecurityRuleResult.UNKNOWN
        }
    }
}