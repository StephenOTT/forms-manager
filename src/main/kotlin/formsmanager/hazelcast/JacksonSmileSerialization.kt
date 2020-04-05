package formsmanager.hazelcast

import com.fasterxml.jackson.dataformat.smile.SmileFactory
import io.micronaut.context.annotation.Context
import io.micronaut.jackson.JacksonConfiguration
import io.micronaut.jackson.ObjectMapperFactory
import javax.inject.Singleton

@Singleton
@Context
class JacksonSmileSerialization(
        private val objectMapperFactory: ObjectMapperFactory,
        private val jacksonConfiguration: JacksonConfiguration
) {
    val smileMapper = objectMapperFactory.objectMapper(jacksonConfiguration, SmileFactory())
}