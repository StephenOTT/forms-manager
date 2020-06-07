package formsmanager.camunda.engine.message.persistence

import com.hazelcast.core.HazelcastInstance
import formsmanager.camunda.engine.message.CamundaMessageBuffer
import formsmanager.camunda.engine.message.MessageWrapper
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.map.persistence.CrudableMapStoreRepository
import formsmanager.core.hazelcast.map.persistence.CurdableMapStore
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import javax.inject.Singleton
import javax.persistence.Entity

@Entity
class MessageWrapperEntity(key: MessageId,
                           classId: String,
                           value: MessageWrapper) : MapStoreEntity<MessageWrapper>(key.toMapKey(), classId, value)


@JdbcRepository(dialect = Dialect.H2)
interface MessageBufferMapStoreRepository : CrudableMapStoreRepository<MessageWrapperEntity>


@Singleton
class MessageBufferMapStore(mapStoreRepository: MessageBufferMapStoreRepository) :
        CurdableMapStore<MessageWrapper, MessageWrapperEntity, MessageBufferMapStoreRepository>(mapStoreRepository)


//@Singleton
//@MapStore(MessageBufferMapStore::class, MessageBufferHazelcastRepository.MAP_NAME)
class MessageBufferHazelcastRepository(
        hazelcastInstance: HazelcastInstance) :
        HazelcastCrudRepository<MessageWrapper>(
                hazelcastInstance = hazelcastInstance,
                mapName = MAP_NAME
        ) {

    companion object {
        const val MAP_NAME = CamundaMessageBuffer.BUFFER_NAME
    }
}