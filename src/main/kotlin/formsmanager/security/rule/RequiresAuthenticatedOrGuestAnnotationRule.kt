package formsmanager.security.rule

import formsmanager.ifDebugEnabled
import formsmanager.security.ShiroMicronautSecurityService
import formsmanager.security.annotation.RequiresAuthenticatedOrGuest
import io.micronaut.http.HttpRequest
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class RequiresAuthenticatedOrGuestAnnotationRule(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService,
        private val validator: ShiroAnnotationRuleValidator
) : SecurityRule {

    private val log = LoggerFactory.getLogger(RequiresAuthenticatedOrGuestAnnotationRule::class.java)

    companion object {
        val ORDER = SecuredAnnotationRule.ORDER - 100
    }

    override fun getOrder(): Int {
        return ORDER
    }

    override fun check(request: HttpRequest<Any>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        return if (routeMatch is MethodBasedRouteMatch<*, *> && routeMatch.hasAnnotation(RequiresAuthenticatedOrGuest::class.java)) {
            log.ifDebugEnabled { "Shrio RequiresAuthenticatedOrGuest annotation was found on route ${routeMatch.name}" }

            val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)
            return if (validator.checkRequiresAuthenticatedOrGuest(subject)) {
                log.ifDebugEnabled { "Allowed: (route: ${routeMatch.name}) Shiro Subject ${subject.principal} is Authenticated or is a Guest" }
                SecurityRuleResult.ALLOWED

            } else {
                log.ifDebugEnabled { "Rejected: (route: ${routeMatch.name}) Shiro Subject must be Authenticated or a Guest (ensure that they are not isRemembered)" }
                SecurityRuleResult.REJECTED
            }
        } else {
            SecurityRuleResult.UNKNOWN
        }
    }
}