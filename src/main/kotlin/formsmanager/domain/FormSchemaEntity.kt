package formsmanager.domain

import com.fasterxml.jackson.annotation.JsonProperty
import formsmanager.core.ConfigField
import formsmanager.core.DataField
import formsmanager.core.EnabledField
import formsmanager.core.TimestampFields
import formsmanager.hazelcast.map.CrudableObject
import formsmanager.respository.FormSchemaEntityWrapper
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
@Introspected
data class FormSchemaEntity(
        override val id: UUID = UUID.randomUUID(),
        override val ol: Long = 0,
        @JsonProperty("form_id") val formId: UUID,
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
        CrudableObject<UUID> {
    override fun toEntityWrapper(): FormSchemaEntityWrapper {
        return FormSchemaEntityWrapper(id, this::class.qualifiedName!!, this)
    }
}

@Schema
data class FormSchemaEntityCreator(
        var ol: Long = 0,

        var schema: FormSchema
){
    fun toFormSchemaEntity(schemaId: UUID, formId: UUID): FormSchemaEntity{
        return FormSchemaEntity(
                id = schemaId,
                ol = ol,
                formId = formId,
                schema = schema
        )
    }
}