package formsmanager.core.hazelcast.map.persistence

import formsmanager.camunda.engine.history.mapstore.MapStoreEntity2
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 * Abstract class used to create MapStore Entities.
 * This base entity creates a wrapper for the value.
 */
@MappedSuperclass
abstract class MapStoreEntity<V: Any>(
        @Id
        val key: String,

        val classId: String,

        @field:TypeDef(type = DataType.BYTE_ARRAY)
        val value: V
): MapStoreEntity2