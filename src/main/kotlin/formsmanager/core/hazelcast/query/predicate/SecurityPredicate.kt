package formsmanager.core.hazelcast.query.predicate

import com.hazelcast.query.Predicate
import formsmanager.Application
import formsmanager.core.hazelcast.context.InjectAware
import formsmanager.core.security.groups.domain.SecurityAware
import formsmanager.core.security.shiro.principal.PrimaryPrincipal
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject
import java.util.*
import javax.inject.Inject

/**
 * How can this have a Security Subject that spans the threads? Would just need to exist the life of the specific query.
 * @param userMapKey The MapKey for the user / subject
 * @param permissionsLogical AND / OR logical operator: used when permission generator returns more than 1 permission that the subject must meet.
 * @param permissionGenerator a function that will return a list of 1 or more Shiro permissions (typical usage is WildcardPermission).
 */
@InjectAware
@Prototype
class SecurityPredicate<T : SecurityAware>(@Parameter private val userMapKey: UUID,
                                           @Parameter private val permissionsLogical: Logical,
                                           @Parameter private val permissionGenerator: (secObject: T) -> List<Permission>) : Predicate<UUID, T> {

    @Inject @Transient
    lateinit var securityManager: SecurityManager

    constructor(@Parameter subject: Subject,
                @Parameter permissionsLogical: Logical,
                @Parameter permissionGenerator: (secObject: T) -> List<Permission>) :
            this((subject.principal as PrimaryPrincipal).userMapKey, permissionsLogical, permissionGenerator) //@TODO review for conversion of subject


    override fun apply(mapEntry: MutableMap.MutableEntry<UUID, T>): Boolean {
        val subjectPrincipal = SimplePrincipalCollection(PrimaryPrincipal(userMapKey), "default") //@TODO review

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