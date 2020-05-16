package formsmanager.core.hazelcast.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.nio.serialization.ByteArraySerializer
import javax.inject.Named
import javax.inject.Singleton

/**
 * ByteArraySerializer for converting objects to ByteArray using Jackson Smile Binary Json.
 */
@Singleton
class SmileByteArraySerializer(
    @param:Named("smile") private val mapper: ObjectMapper
) : ByteArraySerializer<Any> {

    override fun getTypeId(): Int {
        return 666
    }

    override fun destroy() {
        // do nothing
    }

    override fun write(`object`: Any): ByteArray {
        val wrapper = DataWrapper(`object`::class.java.canonicalName!!, `object`)
        return mapper.writeValueAsBytes(wrapper)
    }

    override fun read(buffer: ByteArray): Any? {
        return mapper.readValue(buffer, DataWrapper::class.java).data
    }
}