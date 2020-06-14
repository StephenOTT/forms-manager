package formsmanager.core.hazelcast.map.persistence

import io.micronaut.data.repository.CrudRepository

interface MapStoreCrudRepository<K : Any, V : Any> : CrudRepository<V, K> {
    fun findByKey(keys: Collection<K>): Collection<V>
    fun listKey(): List<K>
    fun deleteByKeyIn(key: List<K>)
}