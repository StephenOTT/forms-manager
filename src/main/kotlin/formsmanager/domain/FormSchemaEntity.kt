package formsmanager.domain

import formsmanager.hazelcast.map.CrudableObject
import formsmanager.respository.FormSchemaEntityWrapper
import java.io.Serializable
import java.time.Instant
import java.util.*

data class FormSchemaEntity(
        override val id: UUID = UUID.randomUUID(),
        override var ol: Long = 0,
        val formId: UUID,
        var schema: FormSchema, //@TODO
        val createdAt: Instant = Instant.now(),
        var updatedAt: Instant = createdAt
): CrudableObject<UUID>, Serializable {
    override fun toEntity(): FormSchemaEntityWrapper {
        return FormSchemaEntityWrapper(id, this::class.qualifiedName!!, this)
    }
}