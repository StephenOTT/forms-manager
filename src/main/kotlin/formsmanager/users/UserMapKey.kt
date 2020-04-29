package formsmanager.users

import formsmanager.core.hazelcast.map.MapKey
import java.util.*

data class UserMapKey(
        val email: String,
        val tenant: UUID
): MapKey {

    /**
     * Returns a v3 UUID based on the email+tenant
     */
    override fun toUUID(): UUID{
        return UUID.nameUUIDFromBytes("${email}${tenant}".toByteArray())
    }
}

