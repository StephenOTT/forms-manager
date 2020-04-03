package formsmanager.domain

import formsmanager.hazelcast.CrudableObject
import formsmanager.respository.FormEntityWrapper
import java.io.Serializable
import java.util.*

data class FormEntity(
        override val id: UUID = UUID.randomUUID(),
        override var ol: Long = 0,
        var name: String,
        var description: String? = null,
        var defaultSchema: UUID? = null
): CrudableObject<UUID>, Serializable {
    override fun toEntity(): FormEntityWrapper {
        return FormEntityWrapper(id, this::class.qualifiedName!!, this)
    }
}