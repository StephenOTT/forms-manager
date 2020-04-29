package formsmanager.core.hazelcast.map

import com.hazelcast.aggregation.Aggregators
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.exception.CrudOperationException
import formsmanager.core.exception.NotFoundException
import formsmanager.core.exception.SomethingWentWrongException
import formsmanager.ifDebugEnabled
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.util.*

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
        hazelcastInstance.getMap<UUID, O>(mapName)
    }

    /**
     * Save a new item
     */
    fun create(item: O): Single<O> {
        return Single.fromFuture(
                mapService.submitToKey(item.getMapKey().toUUID(), CreateEntryProcessor<UUID, O>(item)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.getMapKey()}.", it))
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
                mapService.submitToKey(item.getMapKey().toUUID(), AdvUpdateEntryProcessor<UUID, O>(item, updateLogic)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.getMapKey()}.", it))
                }.doOnSuccess {
                    log.ifDebugEnabled { "Entity Updated: $it" }
                }
    }

    fun exists(mapKey: UUID): Single<Boolean> {
        return Single.fromCallable {
            mapService.containsKey(mapKey)
        }
    }

    fun delete(mapKey: UUID): Single<O> {
        return Single.fromCallable {
            lockItem(mapKey) //@TODO Review for removal and replace with entity processor
        }.map {
            mapService.removeAsync(mapKey).toCompletableFuture()
        }.flatMap {
            Single.fromFuture(it)
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity deleted: $it" }
        }
    }

    fun get(mapKey: UUID): Single<O> {
        return Single.fromFuture(mapService.getAsync(mapKey).toCompletableFuture())
                .onErrorResumeNext {
                    if (it is NullPointerException) {
                        Single.error(NotFoundException("Cannot find item with Id $mapKey", it))
                    } else {
                        Single.error(SomethingWentWrongException(cause = it))
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
    fun count(predicate: Predicate<UUID, O>): Long {
        return mapService.aggregate(Aggregators.count(), predicate)
    }

    fun querySql(sql: String): Collection<O>{
        return mapService.values(Predicates.sql(sql))
    }


    /**
     * Returns true if locked, returns false if could not lock
     */
    private fun lockItem(mapKey: UUID): Boolean {
        return kotlin.runCatching { mapService.tryLock(mapKey) }.getOrElse {
            throw NotFoundException("Error locking item; could not find ${mapKey}.", it)
        }
    }

}