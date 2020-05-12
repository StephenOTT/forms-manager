package formsmanager.core.hazelcast.map.persistence

import com.hazelcast.map.MapStore
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.ifDebugEnabled
import org.slf4j.LoggerFactory
import java.util.stream.Collectors
import java.util.stream.StreamSupport

/**
 * Abstract class used to for creating a MapStore for crud operations with
 * a persistence repositories such as JdbcRepository.
 */
abstract class CurdableMapStore<E : CrudableObject, W : MapStoreEntity<E>, R : CrudableMapStoreRepository<W>>(
        private val mapStoreRepository: R
) : MapStore<String, E> {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun storeAll(map: MutableMap<String, out E>) {
        log.ifDebugEnabled { "storeAll() called for ${this::class.qualifiedName}" }
        // Batches of 500 inserts.  When the batch is successful, the batch is removed from the Map.
        // Hazelcast will retry only on the items left in the method's map.
        map.entries.chunked(500).forEach { batch ->
            mapStoreRepository.saveAll(batch.map {
                it.value.toEntityWrapper() as W
            })
            map.keys.removeAll(batch.map { it.key })
        }
    }

    override fun loadAllKeys(): MutableIterable<String> {
        log.ifDebugEnabled { "loadAllKeys() called for ${this::class.qualifiedName}" }
        return mapStoreRepository.listKey().toMutableList()
    }

    override fun store(key: String, value: E) {
        log.ifDebugEnabled { "store() called for ${this::class.qualifiedName}" }
        mapStoreRepository.save(value.toEntityWrapper() as W)
    }

    override fun loadAll(keys: MutableCollection<String>): MutableMap<String, E> {
        log.ifDebugEnabled { "loadAll() called for ${this::class.qualifiedName}" }
        return StreamSupport.stream(mapStoreRepository.findAll().spliterator(), true)
                .map {
                    it.value
                }.collect(Collectors.toMap({ it.id.toMapKey() }, { it }))
    }

    override fun deleteAll(keys: MutableCollection<String>) {
        log.ifDebugEnabled { "deleteAll() called for ${this::class.qualifiedName}" }
        keys.chunked(1000).forEach { batch ->
            mapStoreRepository.deleteByKeyIn(batch)
            keys.removeAll(batch)
        }
    }

    override fun load(key: String): E? {
        log.ifDebugEnabled { "load() called for ${this::class.qualifiedName}" }
        val loaded = mapStoreRepository.findById(key)
        return if (loaded.isPresent) {
            loaded.get().value
        } else {
            null
        }
    }

    override fun delete(key: String) {
        log.ifDebugEnabled { "delete() called for ${this::class.qualifiedName}" }
        mapStoreRepository.deleteById(key)
    }
}