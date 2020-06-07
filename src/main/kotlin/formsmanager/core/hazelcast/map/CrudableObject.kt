package formsmanager.core.hazelcast.map

import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import net.minidev.json.annotate.JsonIgnore

/**
 * Base interface to define objects that are to be used for CRUD style operations.
 * Used with CrudableMapStore.
 *
 * This interface is applied on domain objects that will be sent into a crudable mapstore.
 * The object will be wrapped in the MapStoreItemWrapper.
 *
 * This interface ensures that there is a Id, a ol/Optimistic Locking id, and a
 * ability to convert the object to a MapStoreItemWrapperEntity.
 *
 */
interface CrudableObject {
    /**
     * Internal ID that never changes.  This represent the constant ID.
     * Typically used for utils like logging and tracking.
     * getMapKey is used as the representation of the key for the user.
     */
    val id: CrudableObjectId<out CrudableObjectId<*>>

    /**
     * Converts object to a Wrapped object used in Database storage (jpa)
     */
    @JsonIgnore
    fun toEntityWrapper(): MapStoreEntity<out CrudableObject>
}

interface OptimisticLocking{
    /**
     * Optimistic locking value
     */
    val ol: Long
}
