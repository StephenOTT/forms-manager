package formsmanager.core.hazelcast.map

import com.hazelcast.aggregation.Aggregators
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.exception.CrudOperationException
import formsmanager.core.exception.NotFoundException
import formsmanager.core.exception.SomethingWentWrongException
import formsmanager.core.ifDebugEnabled
import io.reactivex.Single
import org.slf4j.LoggerFactory

/**
 * Abstract class for creating Hazelcast based Map Based Crud operation based repositories.
 * Used as a helper to quickly setup the map configuration.
 */
abstract class HazelcastCrudRepository<O : CrudableObject>(
        val mapName: String,
        private val hazelcastInstance: HazelcastInstance
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Data source for item in a Hazelcast Jet IMap.
     */
    val mapService by lazy {
        // implemented as a lazy to accommodate circular deps that cause deadlock: https://github.com/micronaut-projects/micronaut-data/issues/464
        hazelcastInstance.getMap<String, O>(mapName)
    }

    /**
     * Save a new item
     */
    fun create(item: O): Single<O> {
        return Single.fromFuture(
                mapService.submitToKey(item.id.toMapKey(), CreateEntryProcessor<String, O>(item)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item::class.qualifiedName} ${item.id.toMapKey()}.", it))
                }.doOnSuccess {
                    log.ifDebugEnabled { "Entity Created: $it" }
                }
    }

    /**
     * Update a existing item.
     */
    fun update(item: O, updateLogic: (originalItem: O, newItem: O) -> O): Single<O> {
        //@TODO Review for refactor optimizations
        return Single.fromFuture(
                mapService.submitToKey(item.id.toMapKey(), AdvUpdateEntryProcessor<String, O>(item, updateLogic)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.id.toMapKey()}.", it))
                }.doOnSuccess {
                    log.ifDebugEnabled { "Entity Updated: $it" }
                }
    }

    fun exists(id: CrudableObjectId<*>): Single<Boolean> {
        return Single.fromCallable {
            mapService.containsKey(id.toMapKey())
        }
    }

    fun delete(id: CrudableObjectId<*>): Single<O> {
        return Single.fromCallable {
            lockItem(id) //@TODO Review for removal and replace with entity processor
        }.map {
            mapService.removeAsync(id.toMapKey()).toCompletableFuture()
        }.flatMap {
            Single.fromFuture(it)
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity deleted: $it" }
        }
    }

    /**
     * @exception NotFoundException If item cannot be found for provided mapkey.
     */
    fun get(id: CrudableObjectId<*>): Single<O> {
        return Single.fromFuture(mapService.getAsync(id.toMapKey()).toCompletableFuture())
                .onErrorResumeNext {
                    if (it is NullPointerException) {
                        Single.error(NotFoundException("Cannot find item with Id ${id.toMapKey()}", it))
                    } else {
                        Single.error(SomethingWentWrongException(cause = it))
                    }
                }
    }

    fun get(predicate: Predicate<String, O>): Single<O>{
        return Single.fromCallable {
            mapService.values(predicate)
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

    fun get(ids: Set<CrudableObjectId<*>>): Single<List<O>> {
        return Single.fromCallable {
            mapService.getAll(ids.map { it.toMapKey() }.toSet()).map {
                it.value
            }
        }.onErrorResumeNext { e ->
            if (e is NullPointerException) {
                Single.error(NotFoundException("Cannot find items ${ids.map{it.toMapKey()}}", e))
            } else {
                Single.error(SomethingWentWrongException(cause = e))
            }
        }
    }

    /**
     * Size of the Map
     */
    fun count(): Int {
        return mapService.count()
    }

    /**
     * Count instances that match predicate
     */
    fun count(predicate: Predicate<String, O>): Long {
        return mapService.aggregate(Aggregators.count(), predicate)
    }

    fun querySql(sql: String): Collection<O> {
        return mapService.values(Predicates.sql(sql))
    }


    /**
     * Returns true if locked, returns false if could not lock
     */
    private fun lockItem(id: CrudableObjectId<*>): Boolean {
        return kotlin.runCatching { mapService.tryLock(id.toMapKey()) }.getOrElse {
            throw NotFoundException("Error locking item; could not find ${id}.", it)
        }
    }

}