package formsmanager.hazelcast.map.persistence

import formsmanager.hazelcast.map.CrudableObject
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.util.*
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 * Abstract class used to create MapStore Entities.
 * This base entity creates a wrapper for the value.
 */
@MappedSuperclass
abstract class MapStoreItemWrapperEntity<V: CrudableObject<UUID>>(
        @Id
        val key: UUID,

        val classId: String,

        @field:TypeDef(type = DataType.BYTE_ARRAY)
        val value: V
)