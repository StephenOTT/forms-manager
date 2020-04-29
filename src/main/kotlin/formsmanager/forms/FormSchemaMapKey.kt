package formsmanager.forms

import formsmanager.core.hazelcast.map.MapKey
import java.util.*

data class FormSchemaMapKey(
        val internalId: UUID

) : MapKey {
    override fun toUUID(): UUID {
        return UUID.nameUUIDFromBytes("$internalId".toByteArray())
    }
}