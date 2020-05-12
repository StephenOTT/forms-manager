package formsmanager.core.hazelcast.map.persistence

import formsmanager.core.hazelcast.map.CrudableObject
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 * Abstract class used to create MapStore Entities.
 * This base entity creates a wrapper for the value.
 */
@MappedSuperclass
abstract class MapStoreEntity<V: CrudableObject>(
        @Id
        val key: String,

        val classId: String,

        @field:TypeDef(type = DataType.BYTE_ARRAY)
        val value: V
)