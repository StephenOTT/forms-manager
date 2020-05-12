package formsmanager.core.hazelcast.query.predicate

import com.hazelcast.query.Predicate
import formsmanager.core.hazelcast.context.InjectAware
import formsmanager.core.security.SecurityAware
import formsmanager.core.security.shiro.principal.PrimaryPrincipal
import io.micronaut.context.annotation.Parameter
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject
import javax.inject.Inject

/**
 * A Predicate used for Shiro Subject Permissions evaluation.
 * Primarily used when doing distributed queries with Hazelcast, and you only want results to be returned that the
 * current Subject has access to.
 *
 * **REQUIRES INJECTION due to bug with Hazelcast: When Predicate is first created, make sure to inject context.
 * Once the Predicate is shipped around to other nodes, Serialization will occur, and injection will occur.**
 *
 * @param userId The MapKey for the user / subject
 * @param permissionsLogical AND / OR logical operator: used when permission generator returns
 * more than 1 permission that the subject must meet.
 * @param permissionGenerator a function that will return a list of 1 or more Shiro permissions
 * (typical usage is WildcardPermission).
 */
@InjectAware
class SecurityPredicate<T : SecurityAware>(@Parameter private val primaryPrincipal: PrimaryPrincipal,
                                           @Parameter private val permissionsLogical: Logical,
                                           @Parameter private val permissionGenerator: (secObject: T) -> List<Permission>) : Predicate<String, T> {

    @Inject
    @Transient
    lateinit var securityManager: SecurityManager

    constructor(@Parameter subject: Subject,
                @Parameter permissionsLogical: Logical,
                @Parameter permissionGenerator: (secObject: T) -> List<Permission>) :
            this((subject.principal as PrimaryPrincipal), permissionsLogical, permissionGenerator) //@TODO review for conversion of subject


    override fun apply(mapEntry: MutableMap.MutableEntry<String, T>): Boolean {
        val subjectPrincipal = SimplePrincipalCollection(primaryPrincipal, "default") //@TODO review

        val permissions: List<Permission> = permissionGenerator.invoke(mapEntry.value)

        require(permissions.isNotEmpty()) {
            "One or more permissions must be returned in permission generator for a SecurityPredicate."
        }

        return when (permissionsLogical) {
            Logical.AND -> {
                securityManager.isPermitted(subjectPrincipal, permissions).all { it }
            }
            Logical.OR -> {
                securityManager.isPermitted(subjectPrincipal, permissions).any { it }
            }
            else -> {
                throw IllegalStateException("Unexpected Logical operator.")
            }
        }
    }
}