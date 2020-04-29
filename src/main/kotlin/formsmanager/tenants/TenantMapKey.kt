package formsmanager.tenants

import formsmanager.core.hazelcast.map.MapKey
import java.util.*

data class TenantMapKey(
        val name: String
): MapKey{
    override fun toUUID(): UUID {
        return UUID.nameUUIDFromBytes(name.toByteArray())
    }
}