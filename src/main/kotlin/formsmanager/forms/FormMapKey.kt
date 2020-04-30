package formsmanager.forms

import formsmanager.core.hazelcast.map.MapKey
import formsmanager.tenants.TenantMapKey
import java.util.*

data class FormMapKey(
        val name: String,
        val tenant: UUID
) : MapKey {

    constructor(name: String, tenantName: String): this(name, TenantMapKey(tenantName).toUUID())

    override fun toUUID(): UUID {
        return UUID.nameUUIDFromBytes("${name}${tenant}".toByteArray())
    }
}