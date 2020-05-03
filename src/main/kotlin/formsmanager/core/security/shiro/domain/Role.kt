package formsmanager.core.security.shiro.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermission

/**
 * Defines a Shrio Role, which contains a the name of the role (typically "ROLE_THEROLENAME", and a Set of Shiro wildcardPermission strings.
 * @param name Role name
 * @param permissions Set of Strings that are Shrio WildcardPermissions
 */
@Schema
data class Role(
        val name: String,
        val permissions: Set<String> = setOf()
): Comparable<Role> {

    fun permissionStringsToWildcardPermissions(): Set<Permission>{
        //@TODO refactor for performance
        return permissions.map {
            WildcardPermission(it)
        }.toSet()
    }


    fun isPermitted(p: Permission): Boolean {
        //@TODO refactor for performance
        return permissionStringsToWildcardPermissions().any {
            it.implies(p)
        }
    }

    /**
     * Compares based on Role name.
     */
    override fun compareTo(other: Role): Int {
        return this.name.compareTo(other.name)
    }
}