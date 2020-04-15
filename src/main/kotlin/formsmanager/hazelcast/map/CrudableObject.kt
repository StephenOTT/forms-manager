package formsmanager.hazelcast.map

import formsmanager.hazelcast.map.persistence.MapStoreItemWrapperEntity

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
interface CrudableObject<I : Any> {
    val id: I
    val ol: Long

    fun toEntityWrapper(): MapStoreItemWrapperEntity<*>
}