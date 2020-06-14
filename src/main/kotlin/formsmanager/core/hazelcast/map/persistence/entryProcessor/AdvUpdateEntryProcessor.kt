package formsmanager.core.hazelcast.map.persistence.entryProcessor

import com.hazelcast.core.Offloadable
import com.hazelcast.map.EntryProcessor
import com.hazelcast.spi.impl.executionservice.ExecutionService
import formsmanager.core.exception.NotFoundException
import formsmanager.core.exception.OptimisticLockingException
import formsmanager.core.hazelcast.map.persistence.OptimisticLocking
import io.micronaut.core.beans.BeanIntrospector
import io.micronaut.core.beans.BeanProperty

class AdvUpdateEntryProcessor<K : Any, V : Any>(private val updateValue: V, private val updateLogic: (originalItem: V, newItem: V) -> V) : EntryProcessor<K, V, V>, Offloadable {
    override fun process(entry: MutableMap.MutableEntry<K, V>): V {
        val value: V? = entry.value

        if (value == null) {
            throw NotFoundException("Item ${entry.key} could not be found.")

        } else {
            // Get the V class from the bean introspector
            val introspection = BeanIntrospector.SHARED.findIntrospection(value::class.java)
            introspection.ifPresent {

                // If the class has a OptimisticLocking annotation on a property, then:
                val olProp = it.beanProperties.single { beanProperty ->
                    beanProperty.hasAnnotation(OptimisticLocking::class.java)
                } as BeanProperty<V, Any>

                // Requires that the old value matches the currently submitted update value
                require(olProp[value] == olProp[updateValue]) {
                    OptimisticLockingException("Optimistic Locking Exception: Unable to update item ${entry.key}")
                }
            }

            entry.setValue(updateLogic.invoke(value, updateValue))
            return entry.value
        }
    }

    override fun getExecutorName(): String {
        return ExecutionService.OFFLOADABLE_EXECUTOR
    }
}