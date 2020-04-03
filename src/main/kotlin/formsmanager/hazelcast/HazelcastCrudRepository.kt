package formsmanager.hazelcast

import formsmanager.exception.CrudOperationException
import formsmanager.exception.NotFoundException
import formsmanager.exception.SomethingWentWrongException
import io.reactivex.Single

open class HazelcastCrudRepository<K : Any, O : CrudableObject<K>>(
        val mapName: String,
        private val jetService: HazelcastJet
) {

    /**
     * Data source for item in a Hazelcast Jet IMap.
     */
    val mapService by lazy {
        // implemented as a lazy to accomodate circular deps that cause deadlock: https://github.com/micronaut-projects/micronaut-data/issues/464
        jetService.jet.getMap<K, O>(mapName)
    }

    /**
     * Save a new item
     */
    fun create(item: O): Single<O> {
        return Single.fromFuture(
                mapService.submitToKey<O>(item.id, CreateEntryProcessor<K, O>(item)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.id}.", it))
                }
    }

    /**
     * Update a existing item.
     */
    fun update(item: O, updateLogic: (originalItem: O) -> O): Single<O> {
        //@TODO Review for refactor optimizations
        return Single.fromFuture(
                mapService.submitToKey<O>(item.id, AdvUpdateEntryProcessor<K, O>(item, updateLogic)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item.id}.", it))
                }
    }

    fun exists(itemKey: K): Single<Boolean> {
        return Single.fromCallable {
            mapService.containsKey(itemKey)
        }
    }

    fun delete(itemKey: K): Single<O> {
        return Single.fromCallable {
            lockItem(itemKey)
        }.map {
            mapService.removeAsync(itemKey).toCompletableFuture()
        }.flatMap {
            Single.fromFuture(it)
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