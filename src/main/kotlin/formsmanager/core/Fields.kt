package formsmanager.core

import java.time.Instant
import java.util.*

interface TenantField{
    val tenant: UUID
}

interface TimestampFields{
    val createdAt: Instant
    val updatedAt: Instant
}

interface DataField{
    val data: Map<String, Any?>
}

interface ConfigField{
    val config: Map<String, Any?>
}

interface OwnerField{
    val owner: UUID
}

interface EnabledField{
    val enabled: Boolean
}