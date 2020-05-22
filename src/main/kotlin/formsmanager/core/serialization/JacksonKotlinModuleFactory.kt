package formsmanager.core.serialization

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Indexed
import io.micronaut.core.util.StringUtils
import io.micronaut.jackson.JacksonConfiguration
import javax.inject.Named
import javax.inject.Singleton

@Factory
@Requirements(
        Requires(classes = [KotlinModule::class]),
        Requires(property = JacksonConfiguration.PROPERTY_MODULE_SCAN, value = StringUtils.FALSE)
)
class KotlinModuleFactory(
        private val kotlinModuleConfiguration: JacksonKotlinModuleConfiguration
) {

    @Named("kotlinModule")
    @Singleton
    @Requires(classes = [KotlinModule::class])
    @Indexed(Module::class)
    fun kotlinModule(): Module {
        return KotlinModule(
                reflectionCacheSize = kotlinModuleConfiguration.reflectionCacheSize,
                nullToEmptyCollection = kotlinModuleConfiguration.nullToEmptyCollection,
                nullToEmptyMap = kotlinModuleConfiguration.nullToEmptyMap,
                nullisSameAsDefault = kotlinModuleConfiguration.nullisSameAsDefault
        )
    }
}