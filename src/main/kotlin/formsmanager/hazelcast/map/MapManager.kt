package formsmanager.hazelcast.map

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import javax.inject.Singleton

@Singleton
class MapManager(
        private val hazelcastInstance: HazelcastInstance
) {

    fun <K: Any, V: Any> getMap(name: String): IMap<K, V> {
        return hazelcastInstance.getMap<K,V>(name)
    }




}