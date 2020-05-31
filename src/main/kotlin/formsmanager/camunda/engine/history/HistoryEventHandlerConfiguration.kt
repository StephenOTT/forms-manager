package formsmanager.camunda.engine.history

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import io.micronaut.core.util.StringUtils

@ConfigurationProperties("camunda.custom.history.handlers")
interface HistoryEventHandlerConfiguration {

    @get:Bindable(defaultValue = StringUtils.FALSE)
    val enabled: Boolean

    @ConfigurationProperties("composite")
    interface CompositeHistoryEventHandlerConfiguration{

        @get:Bindable(defaultValue = StringUtils.FALSE)
        val enabled: Boolean

        @get:Bindable(defaultValue = StringUtils.TRUE)
        val useDbComposite: Boolean
    }
}