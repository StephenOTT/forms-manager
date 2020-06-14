package formsmanager.camunda.engine.history.mapstore

import formsmanager.core.hazelcast.map.persistence.MapValue
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class GenericMapStoreEntity<V : Any>(
        @Id
        val key: String,

        val classId: String,

        @field:TypeDef(type = DataType.BYTE_ARRAY)
        @field:MapValue
        val value: V
)