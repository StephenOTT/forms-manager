package formsmanager.hazelcast.serialization

import com.hazelcast.nio.serialization.ByteArraySerializer
import javax.inject.Singleton

/**
 * ByteArraySerializer for converting objects to ByteArray using Jackson Smile Binary Json.
 */
@Singleton
class SmileByteArraySerializer(
        private val binaryMapper: JacksonSmileSerialization
) : ByteArraySerializer<Any> {

    private val smileMapper = binaryMapper.smileMapper

    override fun getTypeId(): Int {
        return 666
    }

    override fun destroy() {
        // do nothing
    }

    override fun write(`object`: Any): ByteArray {
        val wrapper = DataWrapper(`object`::class.java.canonicalName!!, `object`)
        return smileMapper.writeValueAsBytes(wrapper)
    }

    override fun read(buffer: ByteArray): Any? {
        return smileMapper.readValue(buffer, DataWrapper::class.java).data
    }
}