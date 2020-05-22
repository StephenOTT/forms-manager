package formsmanager.core.serialization

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Indexed
import io.micronaut.core.util.StringUtils
import io.micronaut.jackson.JacksonConfiguration
import javax.inject.Named
import javax.inject.Singleton


@Factory
class JacksonJava8ModulesFactory {

    @Named("javaTimeModule")
    @Singleton
    @Requirements(
            Requires(classes = [JavaTimeModule::class]),
            Requires(property = JacksonConfiguration.PROPERTY_MODULE_SCAN, value = StringUtils.FALSE)
//            Requires(property = "jackson.useJavaTimeModule", value = StringUtils.TRUE)
    )
    @Indexed(Module::class)
    fun javaTimeModule(): Module {
        return JavaTimeModule()
    }

    /**
     * Provides the JDK 8 module.
     *
     * @return The JDK 8 module
     */
    @Named("jdk8Module")
    @Singleton
    @Requirements(
            Requires(classes = [Jdk8Module::class]),
            Requires(property = JacksonConfiguration.PROPERTY_MODULE_SCAN, value = StringUtils.FALSE)
//            Requires(property = "jackson.useJdk8Module", value = StringUtils.TRUE)
    )
    @Indexed(Module::class)
    fun jdk8Module(): Module {
        return Jdk8Module()
    }
}