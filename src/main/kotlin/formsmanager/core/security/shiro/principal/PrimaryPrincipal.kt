package formsmanager.core.security.shiro.principal

import java.io.Serializable
import java.util.*

/**
 * Provides a Shiro Principal for storage of primary principal info.
 * The first principal in the list of principals is considered the Shrio primary principal.
 */
data class PrimaryPrincipal(
        val userMapKey: UUID
): Serializable