package formsmanager.forms

import formsmanager.core.hazelcast.map.MapKey
import java.util.*

data class FormMapKey(
        val name: String,
        val tenant: UUID
) : MapKey {
    override fun toUUID(): UUID {
        return UUID.nameUUIDFromBytes("${name}${tenant}".toByteArray())
    }
}