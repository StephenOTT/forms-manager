package formsmanager.hazelcast.map

import formsmanager.exception.CrudOperationException
import formsmanager.exception.NotFoundException
import formsmanager.exception.SomethingWentWrongException
import formsmanager.hazelcast.HazelcastJetManager
import formsmanager.ifDebugEnabled
import io.reactivex.Single
import org.slf4j.LoggerFactory

/**
 * Abstract class for creating Hazelcast based Map Based Crud operation based repositories.
 * Used as a helper to quickly setup the map configuration.
 */
abstract class HazelcastCrudRepository<K : Any, O : CrudableObject<K>>(
        val mapName: String,
        private val jetService: HazelcastJetManager
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Data source for item in a Hazelcast Jet IMap.
     */
    val mapService by lazy {
        // implemented as a lazy to accommodate circular deps that cause deadlock: https://github.com/micronaut-projects/micronaut-data/issues/464
        jetService.defaultInstance.getMap<K, O>(mapName)
    }

    /**
     * Save a new item
     */
    fun create(item: O): Single<O> {
        return Single.fromFuture(
                mapService.submitToKey<O>(item.id, CreateEntryProcessor<K, O>(item)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.id}.", it))
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
                mapService.submitToKey<O>(item.id, AdvUpdateEntryProcessor<K, O>(item, updateLogic)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.id}.", it))
                }.doOnSuccess {
                    log.ifDebugEnabled { "Entity Updated: $it" }
                }
    }

    fun exists(itemKey: K): Single<Boolean> {
        return Single.fromCallable {
            mapService.containsKey(itemKey)
        }
    }

    fun delete(itemKey: K): Single<O> {
        return Single.fromCallable {
            lockItem(itemKey) //@TODO Review for removal and replace with entity processor
        }.map {
            mapService.removeAsync(itemKey).toCompletableFuture()
        }.flatMap {
            Single.fromFuture(it)
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity deleted: $it" }
        }
    }

    fun find(itemKey: K): Single<O> {
        return Single.fromFuture(mapService.getAsync(itemKey).toCompletableFuture())
                .onErrorResumeNext {
                    if (it is NullPointerException) {
                        Single.error(NotFoundException("Cannot find item with Id $itemKey", it))
                    } else {
                        Single.error(SomethingWentWrongException(cause = it))
                    }
                }
    }


    /**
     * Returns true if locked, returns false if could not lock
     */
    private fun lockItem(itemKey: K): Boolean {
        return kotlin.runCatching { mapService.tryLock(itemKey) }.getOrElse {
            throw NotFoundException("Error locking item; could not find ${itemKey}.", it)
        }
    }

}