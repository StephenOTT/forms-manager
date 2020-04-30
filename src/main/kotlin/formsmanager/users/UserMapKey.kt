package formsmanager.users

import formsmanager.core.hazelcast.map.MapKey
import formsmanager.tenants.TenantMapKey
import java.util.*

data class UserMapKey(
        val email: String,
        val tenant: UUID
): MapKey {

    constructor(email: String, tenantName: String): this(email, TenantMapKey(tenantName).toUUID())

    /**
     * Returns a v3 UUID based on the email+tenant
     */
    override fun toUUID(): UUID{
        return UUID.nameUUIDFromBytes("${email}${tenant}".toByteArray())
    }
}

