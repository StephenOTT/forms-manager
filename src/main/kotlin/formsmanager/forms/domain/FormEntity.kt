package formsmanager.forms.domain

import formsmanager.core.*
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.hazelcast.map.MapKey
import formsmanager.forms.FormMapKey
import formsmanager.forms.respository.FormEntityWrapper
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class FormEntity(

        override val internalId: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val defaultSchema: UUID? = null,

        val type: FormType = FormType.FORMIO,

        override val tenant: UUID,

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
        TenantField,
        CrudableObject {
    override fun toEntityWrapper(): FormEntityWrapper {
        return FormEntityWrapper(getMapKey(), this::class.qualifiedName!!, this)
    }

    override fun getMapKey(): FormMapKey {
        return FormMapKey(name, tenant)
    }
}

@Schema
data class FormEntityCreator(
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
     * Convert to FormEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toFormEntity(internalId: UUID = UUID.randomUUID(), tenantMapKey: UUID): FormEntity {
        return FormEntity(
                internalId = internalId,
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

@Schema
data class FormEntityModifier(
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
     * Convert to FormEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toFormEntity(internalId: UUID, tenantMapKey: UUID): FormEntity {
        return FormEntity(
                internalId = internalId,
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