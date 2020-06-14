package formsmanager.core.hazelcast.map.persistence

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.MapLoaderLifecycleSupport
import com.hazelcast.map.MapStore
import formsmanager.core.ifDebugEnabled
import io.micronaut.core.beans.BeanIntrospector
import io.micronaut.core.convert.ConversionService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import javax.persistence.Id

class DatabaseMapStore(
        val mapName: String,
        val mapKeyClass: Class<Any>,
        val mapValueClass: Class<Any>,
        val entityClass: Class<Any>,
        val repository: MapStoreCrudRepository<Any, Any>,
        val conversionService: ConversionService<*>
) : MapStore<Any, Any>, MapLoaderLifecycleSupport {

    private val log = LoggerFactory.getLogger("DatabaseMapStore:${mapName}")

    override fun destroy() {
    }

    override fun init(hazelcastInstance: HazelcastInstance, properties: Properties, mapName: String) {
    }

    override fun storeAll(map: MutableMap<Any, Any>) {
        log.ifDebugEnabled { "$mapName : storeAll() called for ${this::class.qualifiedName}" }

        val entities = map.map {
            entityClass.cast(conversionService.convertRequired(it, entityClass))
        }

        repository.saveAll(entities)
    }

    override fun loadAllKeys(): MutableIterable<Any> {
        log.ifDebugEnabled { "$mapName : loadAllKeys() called for ${this::class.qualifiedName}" }
        return repository.listKey().toMutableList()
    }

    override fun store(key: Any, value: Any?) {
        log.ifDebugEnabled { "$mapName : store() called for ${this::class.qualifiedName}" }

        val entity = entityClass.cast(conversionService.convertRequired(value, entityClass))

        repository.save(entity)
    }

    override fun loadAll(keys: MutableCollection<Any>): MutableMap<Any, Any?> {
        log.ifDebugEnabled { "$mapName : loadAll() called for ${this::class.qualifiedName}" }

        return StreamSupport.stream(repository.findByKey(keys).spliterator(), true)
                .collect(Collectors.toMap(
                        {
                            val idProp = BeanIntrospector.SHARED.getIntrospection(entityClass).beanProperties.single { beanProp ->
                                beanProp.hasAnnotation(Id::class.java)
                            }
                            val beanValue = idProp[it]
                            require(beanValue != null)
                            check(mapKeyClass.isAssignableFrom(beanValue::class.java))
                            beanValue
                        },
                        {
                            val idProp = BeanIntrospector.SHARED.getIntrospection(entityClass).beanProperties.single { beanProp ->
                                beanProp.hasAnnotation(MapValue::class.java)
                            }
                            val beanValue = idProp[it]
                            require(beanValue != null)
                            check(mapValueClass.isAssignableFrom(beanValue::class.java ))
                            beanValue
                        }))
    }

    override fun deleteAll(keys: MutableCollection<Any>) {
        log.ifDebugEnabled { "$mapName : deleteAll() called for ${this::class.qualifiedName}" }
        keys.chunked(1000).forEach { batch ->
            repository.deleteByKeyIn(batch)
            keys.removeAll(batch)
        }
    }

    override fun load(key: Any): Any? {
        log.ifDebugEnabled { "$mapName : load() called for ${this::class.qualifiedName}" }
        val loaded = repository.findById(key)

        return if (loaded.isPresent) {
            val idProp = BeanIntrospector.SHARED.getIntrospection(entityClass).beanProperties.single { beanProp ->
                beanProp.hasAnnotation(MapValue::class.java)
            }
            val beanValue = idProp[loaded.get()]
            require(beanValue != null)
            check(mapValueClass.isAssignableFrom(beanValue::class.java))
            beanValue

        } else {
            null
        }
    }

    override fun delete(key: Any) {
        log.ifDebugEnabled { "$mapName : delete() called for ${this::class.qualifiedName}" }
        repository.deleteById(key)
    }
}