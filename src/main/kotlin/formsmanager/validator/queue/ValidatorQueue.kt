package formsmanager.validator.queue

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import com.hazelcast.nio.serialization.ByteArraySerializer
import formsmanager.hazelcast.StandardMessageBusManager
import formsmanager.validator.FormSubmission
import io.micronaut.context.annotation.Context
import io.micronaut.jackson.JacksonConfiguration
import io.micronaut.jackson.ObjectMapperFactory
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Singleton


@Singleton
@Context
class ValidatorProcessor(
        private val mb: StandardMessageBusManager
){
    @PostConstruct
    fun consume(){
        println("setting up consumer!!")
        mb.consumer<FormSubmission>("form-submission-validation"){
            println("consumed received!!!")
            println(it.messageObject.message.toString())
        }
    }
}


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
interface HazelcastTransportable {

}

data class TaskWrapper<T : HazelcastTransportable>(
        val correlationId: UUID,
        val callback: UUID?,
        val type: String,
        val task: T
) : HazelcastTransportable


@Singleton
@Context
class JacksonSmileMapper(
        private val objectMapperFactory: ObjectMapperFactory,
        private val jacksonConfiguration: JacksonConfiguration
) {
    val smileMapper = objectMapperFactory.objectMapper(jacksonConfiguration, SmileFactory())
}

@Singleton
class HazelcastTransportableSmileSerializer(
        private val jacksonSmileMapper: JacksonSmileMapper
) : ByteArraySerializer<HazelcastTransportable> {

    private val smileMapper = jacksonSmileMapper.smileMapper

    override fun getTypeId(): Int {
        return 666
    }

    override fun destroy() {

    }

    override fun write(`object`: HazelcastTransportable): ByteArray {
        return smileMapper.writeValueAsBytes(`object`)
    }

    override fun read(buffer: ByteArray): HazelcastTransportable {
        return smileMapper.readValue(buffer, HazelcastTransportable::class.java)
    }


}