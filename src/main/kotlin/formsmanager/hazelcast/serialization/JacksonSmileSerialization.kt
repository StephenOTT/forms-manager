package formsmanager.hazelcast.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import io.micronaut.context.annotation.Context
import io.micronaut.jackson.JacksonConfiguration
import io.micronaut.jackson.ObjectMapperFactory
import javax.inject.Singleton

/**
 * Setups Jackson Smile serialization.
 */
@Singleton
@Context
class JacksonSmileSerialization(
        private val objectMapperFactory: ObjectMapperFactory,
        private val jacksonConfiguration: JacksonConfiguration
) {
    val smileMapper: ObjectMapper = objectMapperFactory
            .objectMapper(jacksonConfiguration, SmileFactory())
}

