package formsmanager.hazelcast

import com.hazelcast.nio.serialization.ByteArraySerializer
import javax.inject.Singleton

@Singleton
class HazelcastTransportableSmileSerializer(
        private val binaryMapper: JacksonSmileSerialization
) : ByteArraySerializer<HazelcastTransportable> {

    private val smileMapper = binaryMapper.smileMapper

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