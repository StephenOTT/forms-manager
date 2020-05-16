package formsmanager.tenants.domain

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.tenants.repository.TenantEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*


data class TenantId(override val value: UUID): CrudableObjectId<TenantId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun type(): String {
        return "tenant"
    }

    override fun compareTo(other: TenantId): Int {
        return value.compareTo(other.value)
    }
}

@Schema
data class Tenant(

        override val id: TenantId,

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

//        val tenantConfigs: TenantConfig,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt

): TimestampFields,
        CrudableObject {

    override fun toEntityWrapper(): TenantEntity {
        return TenantEntity(id, this::class.qualifiedName!!, this)
    }

}

//@TODO REVIEW why these are var
@Schema
data class TenantCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        val tenantConfigs: TenantConfig
){

    fun toTenant(id: TenantId = TenantId(UuidUtil.newSecureUUID())): Tenant{
        return Tenant(
                id = id,
                ol = ol,
                name = name,
                description = description
//                tenantConfigs = tenantConfigs
        )
    }
}

@Schema
data class TenantModifier(
        var ol: Long = 0,

        val name: String,

        var description: String? = null,

        val tenantConfigs: TenantConfig
){

    fun toTenant(id: TenantId): Tenant{
        return Tenant(
                id = id,
                ol = ol,
                name = name,
                description = description
//                tenantConfigs = tenantConfigs
        )
    }
}