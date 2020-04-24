package formsmanager.tenants.domain

import formsmanager.core.*
import formsmanager.hazelcast.map.CrudableObject
import formsmanager.tenants.repository.TenantEntityWrapper
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class TenantEntity(

        override val id: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        override val owner: UUID,

        override val enabled: Boolean = true
): TimestampFields,
        DataField,
        ConfigField,
        OwnerField,
        EnabledField,
        CrudableObject<UUID> {
    override fun toEntityWrapper(): TenantEntityWrapper {
        return TenantEntityWrapper(id, this::class.qualifiedName!!, this)
    }
}

@Schema
data class TenantEntityCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        var defaultSchema: UUID? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val owner: UUID,

        val enabled: Boolean = true
){
    /**
     * Convert to TenantEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toTenantEntity(id: UUID): TenantEntity{
        return TenantEntity(
                id = id,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                owner = owner,
                enabled = enabled
        )
    }
}