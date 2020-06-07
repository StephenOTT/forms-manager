package formsmanager.core.hazelcast.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.InputChunked
import com.esotericsoftware.kryo.io.OutputChunked
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

internal data class KryoContext(
        val kryo: Kryo,
        val inputChunked: InputChunked,
        val outputChunked: OutputChunked
)

@Singleton
@Requires(property = "kryo.enabled", value = StringUtils.TRUE)
class KyroUtils(
        private val cfg: KryoConfiguration
){

    fun createKryoInstance(): Kryo {
        val kryo = Kryo()
        kryo.isRegistrationRequired = cfg.classRegistrationRequired
        kryo.warnUnregisteredClasses = cfg.warnUnregisteredClasses
        kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
        return kryo
    }

    fun createOutputChunked(): OutputChunked {
        return OutputChunked(cfg.bufferSize)
    }

    fun createInputChunked(): InputChunked {
        return InputChunked(cfg.bufferSize)
    }

}


@Singleton
@Requires(property = "kryo.enabled", value = StringUtils.TRUE)
class KryoStreamSerializer(
    private val kryoUtils: KyroUtils
) : StreamSerializer<Any?> {

    private val KRYOS: ThreadLocal<KryoContext> = object : ThreadLocal<KryoContext>() {
        override fun initialValue(): KryoContext {
            val kryo: Kryo = kryoUtils.createKryoInstance()
            val output = kryoUtils.createOutputChunked()
            val input = kryoUtils.createInputChunked()
            return KryoContext(kryo, input, output)
        }
    }

    override fun getTypeId(): Int {
        return 99
    }

    override fun destroy() {
    }

    override fun write(out: ObjectDataOutput, `object`: Any?) {
        val (kryo, _, output) = KRYOS.get()
        output.outputStream = out as OutputStream
        kryo.writeClassAndObject(output, `object`)
        output.endChunk()
        output.flush()
    }

    override fun read(`in`: ObjectDataInput): Any? {
        val (kryo, input) = KRYOS.get()
        input.inputStream = `in` as InputStream
        return kryo.readClassAndObject(input)
    }
}