package formsmanager.core.security.roles

import formsmanager.core.hazelcast.map.MapKey
import formsmanager.tenants.TenantMapKey
import java.util.*

data class RoleMapKey(
        val name: String,
        val tenant: UUID
): MapKey {

    constructor(name: String, tenantName: String): this(name, TenantMapKey(tenantName).toUUID())

    override fun toUUID(): UUID{
        return UUID.nameUUIDFromBytes("${name}${tenant}".toByteArray())
    }
}