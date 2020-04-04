package formsmanager.hazelcast.map

import com.hazelcast.map.MapStore
import formsmanager.ifDebugEnabled
import formsmanager.validator.queue.HazelcastTransportable
import formsmanager.validator.queue.JacksonSmileMapper
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.data.repository.CrudRepository
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import javax.inject.Singleton
import javax.persistence.Id
import javax.persistence.MappedSuperclass

interface CrudableMapStoreRepository<E: MapStoreItemWrapperEntity<*>> : CrudRepository<E, UUID> {
    fun listKey(): List<UUID>
    fun deleteByKeyIn(key: List<UUID>)
}

@MappedSuperclass
abstract class MapStoreItemWrapperEntity<V: CrudableObject<UUID>>(
        @field:Id
        val key: UUID,

        val classId: String,

        @field:TypeDef(type = DataType.BYTE_ARRAY)
        val value: V
)


@Singleton
class CrudableObjectByteArrayConverter(
        private val smileMapper: JacksonSmileMapper
) : TypeConverter<CrudableObject<*>, ByteArray> {

    private val mapper = smileMapper.smileMapper

    override fun convert(`object`: CrudableObject<*>, targetType: Class<ByteArray>, context: ConversionContext): Optional<ByteArray> {
        return Optional.of(mapper.writeValueAsBytes(`object`))
    }
}

@Singleton
class HazelcastTransportableByteArrayConverter(
        private val smileMapper: JacksonSmileMapper
) : TypeConverter<HazelcastTransportable, ByteArray> {

    private val mapper = smileMapper.smileMapper

    override fun convert(`object`: HazelcastTransportable, targetType: Class<ByteArray>, context: ConversionContext): Optional<ByteArray> {
        return Optional.of(mapper.writeValueAsBytes(`object`))
    }
}


open class CurdableMapStore<E: CrudableObject<UUID>, W: MapStoreItemWrapperEntity<E>, R: CrudableMapStoreRepository<W>>(
        private val mapStoreRepository: R
) : MapStore<UUID, E> {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun storeAll(map: MutableMap<UUID, out E>) {
        log.ifDebugEnabled {"storeAll() called for ${this::class.qualifiedName}"}
        // Batches of 500 inserts.  When the batch is successful, the batch is removed from the Map.
        // Hazelcast will retry only on the items left in the method's map.
        map.entries.chunked(500).forEach { batch->
            mapStoreRepository.saveAll(batch.map {
                it.value.toEntity() as W
            })
            map.keys.removeAll(batch.map{it.key})
        }
    }

    override fun loadAllKeys(): MutableIterable<UUID> {
        log.ifDebugEnabled { "loadAllKeys() called for ${this::class.qualifiedName}" }
        return mapStoreRepository.listKey().toMutableList()
    }

    override fun store(key: UUID, value: E) {
        log.ifDebugEnabled { "store() called for ${this::class.qualifiedName}" }
        mapStoreRepository.save(value.toEntity() as W)
    }

    override fun loadAll(keys: MutableCollection<UUID>?): MutableMap<UUID, E> {
        log.ifDebugEnabled { "loadAll() called for ${this::class.qualifiedName}" }
        return StreamSupport.stream(mapStoreRepository.findAll().spliterator(), true)
                .map { it.value }.collect(Collectors.toMap({ it.id }, { it }))
    }

    override fun deleteAll(keys: MutableCollection<UUID>) {
        log.ifDebugEnabled { "deleteAll() called for ${this::class.qualifiedName}" }
        keys.chunked(1000).forEach { batch ->
            mapStoreRepository.deleteByKeyIn(batch)
            keys.removeAll(batch)
        }
    }

    override fun load(key: UUID): E? {
        log.ifDebugEnabled { "load() called for ${this::class.qualifiedName}" }
        val loaded = mapStoreRepository.findById(key)
        return if (loaded.isPresent) {
            loaded.get().value
        } else {
            null
        }
    }

    override fun delete(key: UUID) {
        log.ifDebugEnabled { "delete() called for ${this::class.qualifiedName}" }
        mapStoreRepository.deleteById(key)
    }
}