package formsmanager.hazelcast.serialization

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hazelcast.nio.serialization.ByteArraySerializer
import javax.inject.Singleton

/**
 * Used as a class type wrapper for Jackson Smile serialization
 */
data class DataWrapper(
        val clazz: String,

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "clazz")
        val data: Any?
)

@Singleton
class HazelcastTransportableSmileSerializer(
        private val binaryMapper: JacksonSmileSerialization
) : ByteArraySerializer<Any> {

    private val smileMapper = binaryMapper.smileMapper

    override fun getTypeId(): Int {
        return 666
    }

    override fun destroy() {

    }

    override fun write(`object`: Any): ByteArray {
        val wrapper = DataWrapper(`object`::class.java.canonicalName!!, `object`)
        return smileMapper.writeValueAsBytes(wrapper)
    }

    override fun read(buffer: ByteArray): Any? {
        return smileMapper.readValue(buffer, DataWrapper::class.java).data
    }
}