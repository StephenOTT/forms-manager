package formsmanager.core.hazelcast.map.persistence

import io.micronaut.data.repository.CrudRepository

/**
 * Base interface for creating Repositories (such as a JdbcRepositoru) that will be used with a MapStore.
 */
interface CrudableMapStoreRepository<E: MapStoreEntity<*>> : CrudRepository<E, String> {
    fun listKey(): List<String>
    fun deleteByKeyIn(key: List<String>)
}