package formsmanager.core

import formsmanager.tenants.domain.TenantId
import java.time.Instant
import java.util.*

interface TenantField{
    val tenant: TenantId
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