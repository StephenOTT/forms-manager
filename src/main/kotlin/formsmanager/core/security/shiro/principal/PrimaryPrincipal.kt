package formsmanager.core.security.shiro.principal

import java.util.*

/**
 * Provides a Shiro Principal for storage of a email and tenant.
 */
data class PrimaryPrincipal(
        val userMapkey: UUID,
        val email: String,
        val tenant: UUID
)