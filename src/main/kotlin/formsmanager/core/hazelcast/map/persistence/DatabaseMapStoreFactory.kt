package formsmanager.core.hazelcast.map.persistence

import com.hazelcast.map.MapStore
import com.hazelcast.map.MapStoreFactory
import io.micronaut.core.convert.ConversionService
import java.util.*
import javax.inject.Singleton

@Singleton
class DatabaseMapStoreFactory(
        private val conversionService: ConversionService<*>,
        private val repos: List<MapStoreCrudRepository<*, *>>
) : MapStoreFactory<Any, Any> {

    override fun newMapStore(mapName: String, properties: Properties): MapStore<Any, Any> {
       return kotlin.runCatching {
            val keyClass: Class<*> = Class.forName(properties.getProperty("keyClass", "java.lang.Object"))
            val valueClass: Class<*> = Class.forName(properties.getProperty("valueClass", "java.lang.Object"))
            val repoClass: Class<*> = Class.forName(properties.getProperty("repoClass"))
            val entityClass: Class<*> = Class.forName(properties.getProperty("entityClass"))

            val repo = repos.single {
                it::class.qualifiedName!!.startsWith(repoClass.canonicalName)
            } as MapStoreCrudRepository<Any, Any>

            createDatabaseMapStore(mapName, keyClass, valueClass, entityClass, repo, conversionService)

        }.getOrElse {
            throw IllegalArgumentException("Unable to build mapstore for map $mapName", it)
        }

    }

    private fun createDatabaseMapStore(mapName: String, key: Class<*>, value: Class<*>, entity: Class<*>, repository: MapStoreCrudRepository<Any, Any>, conversionService: ConversionService<*>): MapStore<Any, Any> {

        return DatabaseMapStore(
                mapName,
                key as Class<Any>,
                value as Class<Any>,
                entity as Class<Any>,
                repository,
                conversionService)
    }

}