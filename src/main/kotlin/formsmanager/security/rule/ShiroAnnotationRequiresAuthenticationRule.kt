package formsmanager.security.rule

import formsmanager.ifDebugEnabled
import formsmanager.security.ShiroMicronautSecurityService
import io.micronaut.http.HttpRequest
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ShiroAnnotationRequiresAuthenticationRule(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService,
        private val validator: ShiroAnnotationRuleValidator
) : SecurityRule {

    private val log = LoggerFactory.getLogger(ShiroAnnotationRequiresAuthenticationRule::class.java)

    companion object {
        val ORDER = SecuredAnnotationRule.ORDER - 100
    }

    override fun getOrder(): Int {
        return ORDER
    }

    override fun check(request: HttpRequest<Any>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        return if (routeMatch is MethodBasedRouteMatch<*, *> && routeMatch.hasAnnotation(RequiresAuthentication::class.java)) {
            log.ifDebugEnabled { "Shrio RequiresAuthentication annotation was found for route ${routeMatch.name}" }

            val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)

            return if (validator.checkRequiresAuthentication(subject)) {
                log.ifDebugEnabled { "Allowed: (route: ${routeMatch.name}) Shiro Subject ${subject.principal} was validated as Authenticated." }
                SecurityRuleResult.ALLOWED
            } else {
                // Is not Authenticated
                log.ifDebugEnabled { "Rejected: (route: ${routeMatch.name}) Shiro Subject was not Authenticated / was detected as Anonymous." }
                SecurityRuleResult.REJECTED
            }
        } else {
            SecurityRuleResult.UNKNOWN
        }
    }
}