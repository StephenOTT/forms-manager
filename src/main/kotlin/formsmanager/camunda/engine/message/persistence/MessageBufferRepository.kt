package formsmanager.camunda.engine.message.persistence

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.engine.history.mapstore.GenericMapStoreEntity
import formsmanager.camunda.engine.message.CamundaMessageBuffer
import formsmanager.camunda.engine.message.MessageWrapper
import formsmanager.core.hazelcast.map.persistence.HazelcastReactiveRepository
import formsmanager.core.hazelcast.map.persistence.MapStoreCrudRepository
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity

@Entity
class MessageWrapperEntity(key: MessageId,
                           classId: String,
                           value: MessageWrapper) : GenericMapStoreEntity<MessageWrapper>(key.toMapKey(), classId, value)


@JdbcRepository(dialect = Dialect.H2)
interface MessageBufferMapStoreRepository : MapStoreCrudRepository<String, MessageWrapperEntity>

@Singleton
class MessageWrapperToEntityTypeConverter : TypeConverter<MessageWrapper, MessageWrapperEntity> {
    override fun convert(`object`: MessageWrapper, targetType: Class<MessageWrapperEntity>, context: ConversionContext): Optional<MessageWrapperEntity> {
        return Optional.of(MessageWrapperEntity(`object`.id, `object`::class.qualifiedName!!, `object`))
    }
}

//@Singleton
//class MessageBufferMapStore(override val conversionService: ConversionService<*>,
//                            override val repository: MessageBufferMapStoreRepository) :
//        GenericMapStore<String, MessageWrapper, MessageWrapperEntity, MessageBufferMapStoreRepository> {
//    override val mapValue: KClass<MessageWrapper> = MessageWrapper::class
//    override val entity: KClass<MessageWrapperEntity> = MessageWrapperEntity::class
//}


@Singleton
//@MapStore(MessageBufferMapStore::class, CamundaMessageBuffer.BUFFER_NAME)
class MessageBufferHazelcastRepository(
        hazelcastInstance: HazelcastInstance
//        @Named(CamundaMessageBuffer.BUFFER_NAME) override val iMap: Provider<IMap<String, MessageWrapper>>
) : HazelcastReactiveRepository<String, MessageWrapper>{
    override val iMap: IMap<String, MessageWrapper> by lazy {
        hazelcastInstance.getMap<String, MessageWrapper>(CamundaMessageBuffer.BUFFER_NAME)
    }
}