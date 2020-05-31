package formsmanager.camunda.engine.deploymentcache

import com.hazelcast.map.IMap
import org.camunda.commons.utils.cache.Cache

class HazelcastCache<T>(
        private val map: IMap<String, T>
) : Cache<String, T> {
    override fun clear() {
        map.clear()
    }

    override fun put(key: String, value: T) {
        map[key] = value
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty
    }

    override fun remove(key: String) {
        map.remove(key)
    }

    override fun keySet(): Set<String> {
        return map.keys
    }

    override fun size(): Int {
        return map.size
    }

    override fun get(key: String): T? {
        return map[key]
    }

}