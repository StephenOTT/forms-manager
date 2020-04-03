package formsmanager.hazelcast

import com.hazelcast.map.EntryProcessor
import formsmanager.exception.AlreadyExistsException
import formsmanager.exception.NotFoundException
import formsmanager.exception.OptimisticLockingException

class CreateEntryProcessor<K: Any, O: CrudableObject<*>>(private val insertValue: O) : EntryProcessor<K, O, O>{
    override fun process(entry: MutableMap.MutableEntry<K, O>): O {
        val value:O? = entry.value
        if (value != null) {
            throw AlreadyExistsException("Item ${entry.key} already exists")
        } else {
            entry.setValue(insertValue)
            return entry.value
        }
    }
}

/**
 * Entry Processor for Creating Map values.
 * Provides ability to pass a function for insert logic with the new object.
 * Use case is to allow insert-time modifications (example a optimistic locking update to a value) or other logic checks.
 * Throw a exception to stop the entry processor.
 * The provided function's return value is the object that will be inserted into the map
 */
class AdvCreateEntryProcessor<K : Any, O : CrudableObject<*>>(private val insertValue: O, private val insertLogic: (insertValue: O) -> O) : EntryProcessor<K, O, O> {
    override fun process(entry: MutableMap.MutableEntry<K, O>): O {
        val value:O? = entry.value
        if (value != null) {
            throw AlreadyExistsException("Item ${entry.key} already exists")
        } else {
            entry.setValue(insertLogic.invoke(insertValue))
            return entry.value
        }
    }
}

/**
 * Entry Processor to be used for Updates to a existing Map Entry.
 * Appliess a +1 to the `ol` propert in the HazelcastCrudableObject
 *
 */
class UpdateEntryProcessor<K : Any, O : CrudableObject<*>>(private val updateValue: O) : EntryProcessor<K, O, O> {
    override fun process(entry: MutableMap.MutableEntry<K, O>): O {
        val value: O? = entry.value
        if (value == null) {
            throw NotFoundException("Item ${entry.key} could not be found.")
        } else {
            //@TODO Review doing a injectable function to write custom modify logic
            entry.setValue(updateValue.apply { ol += 1 })
            return entry.value
        }
    }
}

/**
 * Entry Processor for Updating existing Map values.
 * Provides ability to pass a function for update logic with the new object and old object.
 * The provided function's return value is the object that will be replace the old object
 */
class AdvUpdateEntryProcessor<K : Any, O : CrudableObject<*>>(private val updateValue: O, private val updateLogic: (originalItem: O) -> O) : EntryProcessor<K, O, O> {
    override fun process(entry: MutableMap.MutableEntry<K, O>): O {
        val value: O? = entry.value
        if (value == null) {
            throw NotFoundException("Item ${entry.key} could not be found.")
        } else {
            if (value.ol != updateValue.ol) {
                throw OptimisticLockingException("Optimistic Locking Exception: Provided item ${updateValue.id} does not have the same OL value of locked item ${value.ol}.")
            }
            entry.setValue(updateLogic.invoke(value))
            return entry.value
        }
    }
}