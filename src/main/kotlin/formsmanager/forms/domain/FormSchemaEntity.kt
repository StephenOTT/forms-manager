package formsmanager.forms.domain

import formsmanager.core.ConfigField
import formsmanager.core.DataField
import formsmanager.core.EnabledField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.forms.FormSchemaMapKey
import formsmanager.forms.respository.FormSchemaEntityWrapper
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
@Introspected
data class FormSchemaEntity(
        override val internalId: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val formId: UUID,

        var schema: FormSchema, //@TODO

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

    override fun toEntityWrapper(): FormSchemaEntityWrapper {
        return FormSchemaEntityWrapper(mapKey(), this::class.qualifiedName!!, this)
    }

    override fun mapKey(): FormSchemaMapKey {
        return FormSchemaMapKey(internalId)
    }
}

@Schema
data class FormSchemaEntityCreator(
        var ol: Long = 0,

        var schema: FormSchema,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val enabled: Boolean = true
){
    fun toFormSchemaEntity(internalId: UUID = UUID.randomUUID(), formMapKey: UUID): FormSchemaEntity {
        return FormSchemaEntity(
                internalId = internalId,
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
data class FormSchemaEntityModifier(
        var ol: Long = 0,

        var schema: FormSchema,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val enabled: Boolean = true
){
    fun toFormSchemaEntity(internalId: UUID, formMapKey: UUID): FormSchemaEntity {
        return FormSchemaEntity(
                internalId = internalId,
                ol = ol,
                formId = formMapKey,
                schema = schema,
                data = data,
                config = config,
                enabled = enabled
        )
    }
}