package formsmanager.core.serialization

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("jackson.kotlinModule")
interface JacksonKotlinModuleConfiguration{

    @get:Bindable(defaultValue = "512")
    val reflectionCacheSize: Int

    @get:Bindable(defaultValue = "false")
    val nullToEmptyCollection: Boolean

    @get:Bindable(defaultValue = "false")
    val nullToEmptyMap: Boolean

    @get:Bindable(defaultValue = "false")
    val nullisSameAsDefault: Boolean

}