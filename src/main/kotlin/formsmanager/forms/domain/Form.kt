package formsmanager.forms.domain

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.*
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.tenants.domain.TenantId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class FormId(override val value: UUID): CrudableObjectId<FormId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun type(): String {
        return "form"
    }

    override fun compareTo(other: FormId): Int {
        return value.compareTo(other.value)
    }
}


@Schema
data class Form(

        val id: FormId,

        val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val defaultSchema: FormSchemaId? = null,

        val type: FormType = FormType.FORMIO,

        override val tenant: TenantId,

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
        TenantField
//        CrudableObject,
//        OptimisticLocking
{
//    override fun toEntityWrapper(): FormEntity {
//        return FormEntity(id, this::class.qualifiedName!!, this)
//    }

}

@Schema
data class FormCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        var defaultSchema: FormSchemaId? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val owner: UUID,

        val enabled: Boolean = true
){
    /**
     * Convert to FormEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toForm(id: FormId = FormId(UuidUtil.newSecureUUID()), tenantId: TenantId): Form {
        return Form(
                id = id,
                ol = ol,
                name = name,
                description = description,
                defaultSchema = defaultSchema,
                tenant = tenantId,
                data = data,
                config = config,
                owner = owner,
                enabled = enabled
        )
    }
}

@Schema
data class FormModifier(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        var defaultSchema: FormSchemaId? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val owner: UUID,

        val enabled: Boolean = true
){
    /**
     * Convert to FormEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toForm(id: FormId, tenantMapKey: TenantId): Form {
        return Form(
                id = id,
                ol = ol,
                name = name,
                description = description,
                defaultSchema = defaultSchema,
                tenant = tenantMapKey,
                data = data,
                config = config,
                owner = owner,
                enabled = enabled
        )
    }
}