package formsmanager.camunda.messagebuffer.processor

import org.springframework.util.backoff.ExponentialBackOff

/**
 * Configuration to build a ExponentialBackoff / BackOffExecution
 * @TODO create a application.yml configuration for this
 */
data class ExponentialBackoffConfiguration(
        val initialInterval: Long = 2000L,
        val multiplier: Double = 1.5,
        val maxInterval: Long = 600000L, // The max retry interval duration
        val maxElapsedTime: Long = 1200000L // The max time it will keep retrying for
){
    fun toExponentialBackOff(): ExponentialBackOff {
        return ExponentialBackOff().also {
            it.initialInterval = initialInterval
            it.multiplier = multiplier
            it.maxInterval = maxInterval
            it.maxElapsedTime = maxElapsedTime
        }
    }
}