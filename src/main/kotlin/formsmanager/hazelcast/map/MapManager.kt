package formsmanager.hazelcast.map

import com.hazelcast.map.IMap
import formsmanager.hazelcast.HazelcastJetManager
import javax.inject.Singleton

@Singleton
class MapManager(
        private val hazelcastJetManager: HazelcastJetManager
) {

    fun <K: Any, V: Any> getMap(name: String): IMap<K, V> {
        return hazelcastJetManager.defaultInstance.getMap<K,V>(name)
    }




}