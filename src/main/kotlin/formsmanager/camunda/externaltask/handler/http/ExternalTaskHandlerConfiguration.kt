package formsmanager.camunda.externaltask.handler.http

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("camunda.reactive.externalTasks")
interface ExternalTaskHandlerConfiguration{

    /**
     * In Seconds, how often to check for closed netty connections.
     */
    @get:Bindable(defaultValue = "15")
    val cleanClosedConnectionsCycle: Long

    /**
     * Backpressure buffer for created events.
     */
    @get:Bindable(defaultValue = "100000")
    val createdEventBuffer: Int

    /**
     * Backpressure buffer for unlocked events.
     */
    @get:Bindable(defaultValue = "100000")
    val unlockedEventBuffer: Int

}