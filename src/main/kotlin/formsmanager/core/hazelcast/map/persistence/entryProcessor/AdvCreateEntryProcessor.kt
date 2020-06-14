package formsmanager.core.hazelcast.map.persistence.entryProcessor

import com.hazelcast.core.Offloadable
import com.hazelcast.map.EntryProcessor
import com.hazelcast.spi.impl.executionservice.ExecutionService
import formsmanager.core.exception.AlreadyExistsException

class AdvCreateEntryProcessor<K : Any, V : Any>(private val insertValue: V, private val insertLogic: (insertValue: V) -> V) : EntryProcessor<K, V, V>, Offloadable {
    override fun process(entry: MutableMap.MutableEntry<K, V>): V {
        val value: V? = entry.value

        if (value != null) {
            throw AlreadyExistsException("Item ${entry.key} already exists")

        } else {
            entry.setValue(insertLogic.invoke(insertValue))
            return entry.value
        }
    }

    override fun getExecutorName(): String {
        return ExecutionService.OFFLOADABLE_EXECUTOR
    }
}