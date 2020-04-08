package formsmanager.hazelcast.map.persistence

import io.micronaut.data.repository.CrudRepository
import java.util.*

/**
 * Base interface for creating Repositories (such as a JdbcRepositoru) that will be used with a MapStore.
 */
interface CrudableMapStoreRepository<E: MapStoreItemWrapperEntity<*>> : CrudRepository<E, UUID> {
    fun listKey(): List<UUID>
    fun deleteByKeyIn(key: List<UUID>)
}