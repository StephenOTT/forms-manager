package formsmanager.tenants.domain

import formsmanager.core.*
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.tenants.TenantMapKey
import formsmanager.tenants.repository.TenantEntityWrapper
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class TenantEntity(

        override val internalId: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf()

): TimestampFields,
        DataField,
        ConfigField,
        CrudableObject {

    override fun toEntityWrapper(): TenantEntityWrapper {
        return TenantEntityWrapper(getMapKey(), this::class.qualifiedName!!, this)
    }

    override fun getMapKey(): TenantMapKey {
        return TenantMapKey(name)
    }
}

@Schema
data class TenantEntityCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf()
){

    fun toTenantEntity(): TenantEntity{
        return TenantEntity(
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config
        )
    }
}

@Schema
data class TenantEntityModifier(
        var ol: Long = 0,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf()
){

    fun toTenantEntity(name: String, internalId: UUID): TenantEntity{
        return TenantEntity(
                internalId = internalId,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config
        )
    }
}