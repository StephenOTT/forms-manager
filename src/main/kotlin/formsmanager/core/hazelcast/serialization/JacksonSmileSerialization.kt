package formsmanager.core.hazelcast.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import io.micronaut.jackson.JacksonConfiguration
import io.micronaut.jackson.ObjectMapperFactory
import javax.inject.Singleton

/**
 * Setups Jackson Smile serialization.
 */
@Singleton
class JacksonSmileSerialization(
        private val objectMapperFactory: ObjectMapperFactory,
        private val jacksonConfiguration: JacksonConfiguration
) {
    val smileMapper: ObjectMapper = objectMapperFactory
            .objectMapper(jacksonConfiguration, SmileFactory())
}

