package formsmanager.core.security.shiro.principal

import formsmanager.tenants.domain.TenantId
import formsmanager.users.domain.UserId
import java.io.Serializable

/**
 * Provides a Shiro Principal for storage of primary principal info.
 * The first principal in the list of principals is considered the Shrio primary principal.
 * Must be serializable for use with SecurityPredicates
 */
data class PrimaryPrincipal(
        val userId: UserId,
        val tenantId: TenantId
): Serializable