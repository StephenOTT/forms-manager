package formsmanager.core.hazelcast.map.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import formsmanager.core.hazelcast.map.CrudableObject
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import java.util.*
import javax.inject.Singleton

/**
 * Used by the default micronaut jackson implementation to convert object to bytes for
 * storage in a database / JdbcRepository
 *
 * Detected by micronaut automatically through singleton annotation.
 */
@Singleton
class CrudableObjectByteArrayConverter(
        private val mapper: ObjectMapper
) : TypeConverter<CrudableObject<*>, ByteArray> {

    override fun convert(`object`: CrudableObject<*>, targetType: Class<ByteArray>, context: ConversionContext): Optional<ByteArray> {
        return Optional.of(mapper.writeValueAsBytes(`object`))
    }
}