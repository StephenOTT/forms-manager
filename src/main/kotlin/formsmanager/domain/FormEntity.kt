package formsmanager.domain

import formsmanager.hazelcast.map.CrudableObject
import formsmanager.respository.FormEntityWrapper
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*
import javax.annotation.Nullable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema
data class FormEntity(

        override val id: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val defaultSchema: UUID? = null,

        val type: FormType = FormType.FORMIO,

        override val tenant: String? = null,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        override val owner: String,

        override val enabled: Boolean = true
): TimestampFields,
        DataField,
        ConfigField,
        OwnerField,
        EnabledField,
        TenantField,
        CrudableObject<UUID> {
    override fun toEntity(): FormEntityWrapper {
        return FormEntityWrapper(id, this::class.qualifiedName!!, this)
    }
}

@Schema
data class FormEntityCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        var defaultSchema: UUID? = null,

        val tenant: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val owner: String,

        val enabled: Boolean = true
){
    fun toFormEntity(id: UUID): FormEntity{
        return FormEntity(
                id = id,
                ol = ol,
                name = name,
                description = description,
                defaultSchema = defaultSchema,
                tenant = tenant,
                data = data,
                config = config,
                owner = owner,
                enabled = enabled
        )
    }
}