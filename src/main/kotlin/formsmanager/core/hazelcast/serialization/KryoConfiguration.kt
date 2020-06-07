package formsmanager.core.hazelcast.serialization

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import io.micronaut.core.util.StringUtils

@ConfigurationProperties("kryo")
interface KryoConfiguration {

    @get:Bindable(defaultValue = StringUtils.FALSE)
    val enabled: Boolean

    @get:Bindable(defaultValue = StringUtils.TRUE)
    val classRegistrationRequired: Boolean

    @get:Bindable(defaultValue = StringUtils.FALSE)
    val warnUnregisteredClasses: Boolean

    @get:Bindable(defaultValue = "16384")
    val bufferSize: Int

}