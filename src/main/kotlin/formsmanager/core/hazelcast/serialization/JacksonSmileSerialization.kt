package formsmanager.core.hazelcast.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import formsmanager.camunda.hazelcast.HistoricVariableInstanceEntitySmileMixIn
import io.micronaut.context.annotation.Factory
import io.micronaut.jackson.JacksonConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Factory
class JsonSmileFactory{
//    https://github.com/micronaut-projects/micronaut-core/issues/3247
//    @Singleton
//    @Named("smile")
//    fun smileFactory(): SmileFactory{
//        return SmileFactory()
//    }

    private val factory = SmileFactory()

    @Singleton
    @Named("smile")
    fun smileObjectMapper(modules: List<com.fasterxml.jackson.databind.Module>,
                          jacksonConfiguration: JacksonConfiguration?): ObjectMapper {
        return setupMapper(
                ObjectMapper(SmileFactory()).registerModules(modules),
                jacksonConfiguration
        )
    }

    private fun setupMapper(mappedInSetup: ObjectMapper, jacksonConfiguration: JacksonConfiguration?): ObjectMapper{
        // SPECIAL MIXIN FOR
        mappedInSetup.addMixIn(HistoricVariableInstanceEntity::class.java, HistoricVariableInstanceEntitySmileMixIn::class.java)

        mappedInSetup.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mappedInSetup.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        mappedInSetup.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true)

        jacksonConfiguration?.let {
            val defaultTyping: DefaultTyping? = jacksonConfiguration.defaultTyping
            if (defaultTyping != null) {
                mappedInSetup.activateDefaultTyping(mappedInSetup.polymorphicTypeValidator, defaultTyping)
            }
            val include: JsonInclude.Include? = jacksonConfiguration.serializationInclusion
            if (include != null) {
                mappedInSetup.setSerializationInclusion(include)
            }
            val dateFormat: String? = jacksonConfiguration.dateFormat
            if (dateFormat != null) {
                mappedInSetup.dateFormat = SimpleDateFormat(dateFormat)
            }
            val locale: Locale? = jacksonConfiguration.locale
            if (locale != null) {
                mappedInSetup.setLocale(locale)
            }
            val timeZone: TimeZone? = jacksonConfiguration.timeZone
            if (timeZone != null) {
                mappedInSetup.setTimeZone(timeZone)
            }
            val propertyNamingStrategy: PropertyNamingStrategy? = jacksonConfiguration.propertyNamingStrategy
            if (propertyNamingStrategy != null) {
                mappedInSetup.propertyNamingStrategy = propertyNamingStrategy
            }
            jacksonConfiguration.serializationSettings.forEach{ (f: SerializationFeature?, state: Boolean?) -> mappedInSetup.configure(f, state!!) }
            jacksonConfiguration.deserializationSettings.forEach { (f: DeserializationFeature?, state: Boolean?) -> mappedInSetup.configure(f, state!!) }
            jacksonConfiguration.mapperSettings.forEach { (f: MapperFeature?, state: Boolean?) -> mappedInSetup.configure(f, state!!) }
            jacksonConfiguration.parserSettings.forEach { (f: JsonParser.Feature?, state: Boolean?) -> mappedInSetup.configure(f, state!!) }
            jacksonConfiguration.generatorSettings.forEach { (f: JsonGenerator.Feature?, state: Boolean?) -> mappedInSetup.configure(f, state!!) }
        }
        return mappedInSetup
    }

}
