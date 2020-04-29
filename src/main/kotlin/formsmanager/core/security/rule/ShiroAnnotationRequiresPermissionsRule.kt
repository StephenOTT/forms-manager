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
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class ShiroAnnotationRequiresPermissionsRule(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService,
        private val validator: ShiroAnnotationRuleValidator
) : SecurityRule {

    private val log = LoggerFactory.getLogger(ShiroAnnotationRequiresPermissionsRule::class.java)

    companion object {
        val ORDER = SecuredAnnotationRule.ORDER - 100
    }

    override fun getOrder(): Int {
        return ORDER
    }

    override fun check(request: HttpRequest<Any>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        return if (routeMatch is MethodBasedRouteMatch<*, *> && routeMatch.hasAnnotation(RequiresPermissions::class.java)) {
            log.ifDebugEnabled { "Shrio RequiresPermissions annotation was found on route ${routeMatch.name}" }

            val value: Optional<Array<String>> = routeMatch.getValue(RequiresPermissions::class.java, Array<String>::class.java)
            val logicalValue: Logical = routeMatch.getValue(RequiresPermissions::class.java, "logical", Logical::class.java).orElse(Logical.AND)

            return if (value.isPresent) {
                //@TODO Add Dynamic permission handling based on arguments within the annotation
                val subject = shiroMicronautSecurityService.getSubjectFromHttpRequest(request)

                val permissionsInAnnList = value.get().asList()

                return if (validator.checkRequiresPermissions(subject, permissionsInAnnList, logicalValue)) {
                    log.ifDebugEnabled { "Allowed: (route: ${routeMatch.name}) Shiro Authenticated Subject ${subject.principal} implies the required permissions: $permissionsInAnnList" }
                    SecurityRuleResult.ALLOWED
                } else {
                    // Permissions were not valid
                    log.ifDebugEnabled { "Rejected: (route: ${routeMatch.name}) Shiro Authenticated Subject ${subject.principal} does not imply the required permissions: $permissionsInAnnList" }
                    SecurityRuleResult.REJECTED
                }
            } else {
                log.error("Rejected: Value property was not present in Shiro RequiresPermissions annotation on route ${routeMatch.name}")
                SecurityRuleResult.REJECTED
            }
        } else {
            SecurityRuleResult.UNKNOWN
        }
    }
}