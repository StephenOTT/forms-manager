package formsmanager.forms.domain

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.ConfigField
import formsmanager.core.DataField
import formsmanager.core.EnabledField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.forms.respository.FormSchemaEntity
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class FormSchemaEntityId(val value: UUID): CrudableObjectId<FormSchemaEntityId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun compareTo(other: FormSchemaEntityId): Int {
        return value.compareTo(other.value)
    }
}

@Schema
@Introspected
data class FormSchema(
        override val id: FormSchemaEntityId,

        override val ol: Long = 0,

        val formId: FormId,

        var schema: FormioFormSchema, //@TODO

        override val createdAt: Instant = Instant.now(),

        override var updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        override val enabled: Boolean = true

) : TimestampFields,
        DataField,
        ConfigField,
        EnabledField,
        CrudableObject {

    override fun toEntityWrapper(): FormSchemaEntity {
        return FormSchemaEntity(id, this::class.qualifiedName!!, this)
    }
}

@Schema
data class FormSchemaCreator(
        var ol: Long = 0,

        var schema: FormioFormSchema,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val enabled: Boolean = true
){
    fun toFormSchema(id: FormSchemaEntityId = FormSchemaEntityId(UuidUtil.newSecureUUID()), formMapKey: FormId): FormSchema {
        return FormSchema(
                id = id,
                ol = ol,
                formId = formMapKey,
                schema = schema,
                data = data,
                config = config,
                enabled = enabled
        )
    }
}


@Schema
data class FormSchemaModifier(
        var ol: Long = 0,

        var schema: FormioFormSchema,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val enabled: Boolean = true
){
    fun toFormSchema(id: FormSchemaEntityId, formMapKey: FormId): FormSchema {
        return FormSchema(
                id = id,
                ol = ol,
                formId = formMapKey,
                schema = schema,
                data = data,
                config = config,
                enabled = enabled
        )
    }
}