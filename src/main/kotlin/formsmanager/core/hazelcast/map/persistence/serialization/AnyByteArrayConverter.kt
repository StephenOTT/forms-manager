package formsmanager.core.hazelcast.map.persistence.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AnyByteArrayConverter(
        @param:Named("db") private val mapper: ObjectMapper
) : TypeConverter<Any, ByteArray> {

    override fun convert(`object`: Any, targetType: Class<ByteArray>, context: ConversionContext): Optional<ByteArray> {
        return Optional.of(mapper.writeValueAsBytes(`object`))
    }
}