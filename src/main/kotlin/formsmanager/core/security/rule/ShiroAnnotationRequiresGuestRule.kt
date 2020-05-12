package formsmanager.core.security.rule

import formsmanager.core.ifDebugEnabled
import formsmanager.core.security.ShiroMicronautSecurityService
import io.micronaut.http.HttpRequest
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import org.apache.shiro.authz.annotation.RequiresGuest
import org.slf4j.LoggerFactory
import javax.inject.Singleton


@Singleton
class ShiroAnnotationRequiresGuestRule(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService,
        private val validator: ShiroAnnotationRuleValidator
) : SecurityRule {

    private val log = LoggerFactory.getLogger(ShiroAnnotationRequiresGuestRule::class.java)

    companion object {
        val ORDER = SecuredAnnotationRule.ORDER - 100
    }

    override fun getOrder(): Int {
        return ORDER
    }

    override fun check(request: HttpRequest<Any>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        return if (routeMatch is MethodBasedRouteMatch<*, *> && routeMatch.hasAnnotation(RequiresGuest::class.java)) {
            log.ifDebugEnabled { "Shrio RequiresGuest annotation was found on route ${routeMatch.name}" }

            val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)

            return if (validator.checkRequiresGuest(subject)) {
                log.ifDebugEnabled { "Allowed: (route: ${routeMatch.name}) Shiro Subject was validated as a Guest / Non-authenticated." }
                SecurityRuleResult.ALLOWED
            } else {
                log.ifDebugEnabled { "Rejected: (route: ${routeMatch.name}) Shiro Subject not Anonymous / was an Authenticated User." }
                SecurityRuleResult.REJECTED
            }
        } else {
            SecurityRuleResult.UNKNOWN
        }
    }
}