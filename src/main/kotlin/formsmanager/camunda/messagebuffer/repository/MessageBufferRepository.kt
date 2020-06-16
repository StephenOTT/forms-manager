package formsmanager.camunda.messagebuffer.repository

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import formsmanager.camunda.messagebuffer.MessageId
import formsmanager.camunda.messagebuffer.MessageWrapper
import formsmanager.core.hazelcast.map.persistence.GenericMapStoreEntity
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

@Singleton
class MessageBufferHazelcastRepository(
        hazelcastInstance: HazelcastInstance
) : HazelcastReactiveRepository<String, MessageWrapper>{

    companion object {
        const val MAP_NAME = "camunda-message-buffer"
    }

    override val iMap: IMap<String, MessageWrapper> by lazy {
        hazelcastInstance.getMap<String, MessageWrapper>(MAP_NAME)
    }
}