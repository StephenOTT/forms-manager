package formsmanager.core.serialization

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import io.micronaut.core.util.StringUtils

@ConfigurationProperties("jackson.kotlinModule")
interface JacksonKotlinModuleConfiguration{

    @get:Bindable(defaultValue = "512")
    val reflectionCacheSize: Int

    @get:Bindable(defaultValue = StringUtils.FALSE)
    val nullToEmptyCollection: Boolean

    @get:Bindable(defaultValue = StringUtils.FALSE)
    val nullToEmptyMap: Boolean

    @get:Bindable(defaultValue = StringUtils.FALSE)
    val nullisSameAsDefault: Boolean

}