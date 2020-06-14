package formsmanager.core.hazelcast.map.persistence

import com.hazelcast.aggregation.Aggregators
import com.hazelcast.map.IMap
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.exception.CrudOperationException
import formsmanager.core.exception.NotFoundException
import formsmanager.core.exception.SomethingWentWrongException
import formsmanager.core.hazelcast.map.persistence.entryProcessor.AdvCreateEntryProcessor
import formsmanager.core.hazelcast.map.persistence.entryProcessor.AdvUpdateEntryProcessor
import formsmanager.core.ifDebugEnabled
import io.reactivex.Single
import org.slf4j.LoggerFactory

interface HazelcastReactiveRepository<K : Any, V : Any> {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    /**
     * Data source for item in a Hazelcast Jet IMap.
     */
    val iMap: IMap<K, V>

    /**
     * Save a new item
     */
    fun create(mapKey: K, item: V, insertLogic: (insertItem: V) -> V = { it }): Single<V> {
        return Single.fromFuture(
                iMap.submitToKey(mapKey, AdvCreateEntryProcessor<K, V>(item, insertLogic)).toCompletableFuture()
        ).onErrorResumeNext {
            Single.error(CrudOperationException("Unable to create ${item::class.qualifiedName} ${mapKey}.", it))
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity Created: $it" }
        }
    }

    /**
     * Update a existing item.
     */
    fun update(id: K, item: V, updateLogic: (originalItem: V, newItem: V) -> V = { _, new -> new }): Single<V> {
        return Single.fromFuture(
                iMap.submitToKey(id, AdvUpdateEntryProcessor<K, V>(item, updateLogic)).toCompletableFuture()
        ).onErrorResumeNext {
            Single.error(CrudOperationException("Unable to create ${id}.", it))
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity Updated: $it" }
        }
    }

    fun exists(mapKey: K): Single<Boolean> {
        return Single.fromCallable {
            iMap.containsKey(mapKey)
        }
    }

    fun delete(id: K): Single<V> {
        return Single.fromCallable {
            lockItem(id) //@TODO Review for removal and replace with entity processor
        }.map {
            iMap.removeAsync(id).toCompletableFuture()
        }.flatMap {
            Single.fromFuture(it)
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity deleted: $it" }
        }
    }

    /**
     * @exception NotFoundException If item cannot be found for provided mapkey.
     */
    fun get(mapKey: K): Single<V> {
        return Single.fromFuture(
                iMap.getAsync(mapKey).toCompletableFuture()
        ).onErrorResumeNext {
            if (it is NullPointerException) {
                Single.error(NotFoundException("Cannot find item with Id $mapKey", it))
            } else {
                Single.error(SomethingWentWrongException(cause = it))
            }
        }
    }

    fun get(predicate: Predicate<K, V>): Single<V> {
        return Single.fromCallable {
            iMap.values(predicate)
        }.map {
            when {
                it.isEmpty() -> {
                    throw IllegalArgumentException("No results found")
                }
                it.size > 1 -> {
                    throw IllegalArgumentException("More than 1 result was returned. Expected only 1 result.")
                }
                else -> {
                    it.single()
                }
            }
        }
    }

    fun get(ids: Set<K>): Single<List<V>> {
        return Single.fromCallable {
            iMap.getAll(ids).map {
                it.value
            }
        }.onErrorResumeNext { e ->
            if (e is NullPointerException) {
                Single.error(NotFoundException("Cannot find items $ids", e))
            } else {
                Single.error(SomethingWentWrongException(cause = e))
            }
        }
    }

    /**
     * Size of the Map
     */
    fun count(): Int {
        return iMap.count()
    }

    /**
     * Count instances that match predicate
     */
    fun count(predicate: Predicate<K, V>): Long {
        return iMap.aggregate(Aggregators.count(), predicate)
    }

    fun querySql(sql: String): Collection<V> {
        return iMap.values(Predicates.sql(sql))
    }


    /**
     * Returns true if locked, returns false if could not lock
     */
    private fun lockItem(id: K): Boolean {
        return kotlin.runCatching { iMap.tryLock(id) }.getOrElse {
            throw NotFoundException("Error locking item; could not find ${id}.", it)
        }
    }

}