package formsmanager.camunda.engine.metrics

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import javax.validation.constraints.Min

@ConfigurationProperties("camunda.bpm.metrics.custom")

interface CamundaMetricsConfiguration {
    @get:Bindable(defaultValue = "900") // 15 min
    @get:Min(1)
    val reportingInterval: Long
}