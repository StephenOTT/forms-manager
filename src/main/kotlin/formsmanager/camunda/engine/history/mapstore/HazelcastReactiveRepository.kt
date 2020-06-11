package formsmanager.camunda.engine.history.mapstore

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.aggregation.Aggregators
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.Offloadable
import com.hazelcast.map.EntryProcessor
import com.hazelcast.map.IMap
import com.hazelcast.map.MapStore
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import com.hazelcast.spi.impl.executionservice.ExecutionService
import formsmanager.core.exception.AlreadyExistsException
import formsmanager.core.exception.CrudOperationException
import formsmanager.core.exception.NotFoundException
import formsmanager.core.exception.SomethingWentWrongException
import formsmanager.core.ifDebugEnabled
import io.micronaut.context.annotation.Factory
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.beans.BeanIntrospector
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import javax.inject.Named
import javax.inject.Singleton
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.reflect.KClass

@Entity
class MyCustomObjectEntity(key: String,
                   classId: String,
                   value: MyCustomObject) : GenericMapStoreEntity<MyCustomObject>(key, classId, value)

@Singleton
class AnyByteArrayConverter(
        @param:Named("db") private val mapper: ObjectMapper
) : TypeConverter<Any, ByteArray> {

    override fun convert(`object`: Any, targetType: Class<ByteArray>, context: ConversionContext): Optional<ByteArray> {
        return Optional.of(mapper.writeValueAsBytes(`object`))
    }
}


@Singleton
@formsmanager.core.hazelcast.annotation.MapStore(MyCustomObjectMapStore::class, "myCustomObject")
class CustomObjectHazelcastRepository(
        @Named("myCustomObject") override val map: IMap<String, MyCustomObject>
) : HazelcastReactiveRepository<String, MyCustomObject> {}

@JdbcRepository(dialect = Dialect.H2)
interface MyCustomObjectCrudRepository : MapStoreCrudRepository<String, MyCustomObjectEntity>

@Singleton
class MyCustomObjectMapStore(
        override val conversionService: ConversionService<*>,
        override val repository: MyCustomObjectCrudRepository
) : GenericMapStore<String, MyCustomObject, MyCustomObjectEntity, MyCustomObjectCrudRepository> {

    override val entity1: KClass<MyCustomObjectEntity> = MyCustomObjectEntity::class

    override val mapValue: KClass<MyCustomObject> = MyCustomObject::class
}

@Introspected
data class MyCustomObject(
        val id: String,
        val name: String,
        val last: String
)

@Singleton
class MyCustomObjectTypeConverter : TypeConverter<MyCustomObject, formsmanager.camunda.engine.history.mapstore.MapStoreEntity2> {
    override fun convert(`object`: MyCustomObject, targetType: Class<formsmanager.camunda.engine.history.mapstore.MapStoreEntity2>, context: ConversionContext): Optional<formsmanager.camunda.engine.history.mapstore.MapStoreEntity2> {
        return Optional.of(MyCustomObjectEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}


@Factory
class CustomObjectFactory {

    @Named("myCustomObject")
    @Singleton
    fun custom(hazelcastInstance: HazelcastInstance): IMap<String, MyCustomObject> {
        return hazelcastInstance.getMap("myCustomObject")
    }
}


interface MapStoreEntity2 {}

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MapValue {}


interface MapStoreCrudRepository<K : Any, V : Any> : CrudRepository<V, K> {
    fun listKey(): List<K>
    fun deleteByKeyIn(key: List<K>)
}

interface GenericMapStore<K : Any, V : Any, E : MapStoreEntity2, R : MapStoreCrudRepository<K, E>> : MapStore<K, V> {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    val conversionService: ConversionService<*>

    val repository: R

    val mapValue: KClass<V>
    val entity1: KClass<E>

    override fun storeAll(map: MutableMap<K, V>) {
        log.ifDebugEnabled { "storeAll() called for ${this::class.qualifiedName}" }

        val entities = map.map {
            conversionService.convert(it, entity1.java)
                    .orElseThrow { throw IllegalStateException("Unable to convert to entity") }
        }

        repository.saveAll(entities)
    }

    override fun loadAllKeys(): MutableIterable<K> {
        log.ifDebugEnabled { "loadAllKeys() called for ${this::class.qualifiedName}" }
        return repository.listKey().toMutableList()
    }

    override fun store(key: K, value: V) {
        log.ifDebugEnabled { "store() called for ${this::class.qualifiedName}" }

        val entity = conversionService.convert(value, entity1.java)
                .orElseThrow { throw IllegalStateException("Unable to convert to entity") }

        repository.save(entity)
    }

    override fun loadAll(keys: MutableCollection<K>): MutableMap<K, V> {
        log.ifDebugEnabled { "loadAll() called for ${this::class.qualifiedName}" }

        return StreamSupport.stream(repository.findAll().spliterator(), true)
                .collect(Collectors.toMap(
                        {
                            val idProp = BeanIntrospector.SHARED.getIntrospection(entity1.java).beanProperties.single { beanProp ->
                                beanProp.hasAnnotation(Id::class.java)
                            }
                            val beanValue = idProp[it]
                            require(beanValue != null)
                            beanValue as K
                        },
                        {
                            val idProp = BeanIntrospector.SHARED.getIntrospection(entity1.java).beanProperties.single { beanProp ->
                                beanProp.hasAnnotation(MapValue::class.java)
                            }
                            val beanValue = idProp[it]
                            require(beanValue != null)
                            check(beanValue::class == mapValue)
                            beanValue as V
                        }))
    }

    override fun deleteAll(keys: MutableCollection<K>) {
        log.ifDebugEnabled { "deleteAll() called for ${this::class.qualifiedName}" }
        keys.chunked(1000).forEach { batch ->
            repository.deleteByKeyIn(batch)
            keys.removeAll(batch)
        }
    }

    override fun load(key: K): V? {
        log.ifDebugEnabled { "load() called for ${this::class.qualifiedName}" }
        val loaded = repository.findById(key)
        return if (loaded.isPresent) {
            val idProp = BeanIntrospector.SHARED.getIntrospection(entity1.java).beanProperties.single { beanProp ->
                beanProp.hasAnnotation(MapValue::class.java)
            }
            val beanValue = idProp[loaded.get()]
            require(beanValue != null)
            check(beanValue::class == mapValue)
            beanValue as V
        } else {
            null
        }
    }

    override fun delete(key: K) {
        log.ifDebugEnabled { "delete() called for ${this::class.qualifiedName}" }
        repository.deleteById(key)
    }
}


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


class AdvUpdateEntryProcessor<K : Any, V : Any>(private val updateValue: V, private val updateLogic: (originalItem: V, newItem: V) -> V) : EntryProcessor<K, V, V>, Offloadable {
    override fun process(entry: MutableMap.MutableEntry<K, V>): V {
        val value: V? = entry.value
        if (value == null) {
            throw NotFoundException("Item ${entry.key} could not be found.")
        } else {
            entry.setValue(updateLogic.invoke(value, updateValue))
            return entry.value
        }
    }

    override fun getExecutorName(): String {
        return ExecutionService.OFFLOADABLE_EXECUTOR
    }
}


interface HazelcastReactiveRepository<K : Any, V : Any> {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    /**
     * Data source for item in a Hazelcast Jet IMap.
     */
    val map: IMap<K, V>

    /**
     * Save a new item
     */
    fun create(id: K, item: V, insertLogic: (insertItem: V) -> V = { it }): Single<V> {
        return Single.fromFuture(
                map.submitToKey(id, AdvCreateEntryProcessor<K, V>(item, insertLogic)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${item::class.qualifiedName} ${id}.", it))
                }.doOnSuccess {
                    log.ifDebugEnabled { "Entity Created: $it" }
                }
    }

    /**
     * Update a existing item.
     */
    fun update(id: K, item: V, updateLogic: (originalItem: V, newItem: V) -> V = { _, new -> new }): Single<V> {
        return Single.fromFuture(
                map.submitToKey(id, AdvUpdateEntryProcessor<K, V>(item, updateLogic)).toCompletableFuture())
                .onErrorResumeNext {
                    Single.error(CrudOperationException("Unable to create ${id}.", it))
                }.doOnSuccess {
                    log.ifDebugEnabled { "Entity Updated: $it" }
                }
    }

    fun exists(id: K): Single<Boolean> {
        return Single.fromCallable {
            map.containsKey(id)
        }
    }

    fun delete(id: K): Single<V> {
        return Single.fromCallable {
            lockItem(id) //@TODO Review for removal and replace with entity processor
        }.map {
            map.removeAsync(id).toCompletableFuture()
        }.flatMap {
            Single.fromFuture(it)
        }.doOnSuccess {
            log.ifDebugEnabled { "Entity deleted: $it" }
        }
    }

    /**
     * @exception NotFoundException If item cannot be found for provided mapkey.
     */
    fun get(id: K): Single<V> {
        return Single.fromFuture(map.getAsync(id).toCompletableFuture())
                .onErrorResumeNext {
                    if (it is NullPointerException) {
                        Single.error(NotFoundException("Cannot find item with Id $id", it))
                    } else {
                        Single.error(SomethingWentWrongException(cause = it))
                    }
                }
    }

    fun get(predicate: Predicate<K, V>): Single<V> {
        return Single.fromCallable {
            map.values(predicate)
        }.map {
            when {
                it.isEmpty() -> {
                    throw IllegalArgumentException("No results found")
                }
                it.size > 1 -> {
                    throw IllegalArgumentException("More than 1 result was returned. Expected only 1 result.")
                }
                else -> {
                    it.single()
                }
            }
        }
    }

    fun get(ids: Set<K>): Single<List<V>> {
        return Single.fromCallable {
            map.getAll(ids).map {
                it.value
            }
        }.onErrorResumeNext { e ->
            if (e is NullPointerException) {
                Single.error(NotFoundException("Cannot find items $ids", e))
            } else {
                Single.error(SomethingWentWrongException(cause = e))
            }
        }
    }

    /**
     * Size of the Map
     */
    fun count(): Int {
        return map.count()
    }

    /**
     * Count instances that match predicate
     */
    fun count(predicate: Predicate<K, V>): Long {
        return map.aggregate(Aggregators.count(), predicate)
    }

    fun querySql(sql: String): Collection<V> {
        return map.values(Predicates.sql(sql))
    }


    /**
     * Returns true if locked, returns false if could not lock
     */
    private fun lockItem(id: K): Boolean {
        return kotlin.runCatching { map.tryLock(id) }.getOrElse {
            throw NotFoundException("Error locking item; could not find ${id}.", it)
        }
    }

}