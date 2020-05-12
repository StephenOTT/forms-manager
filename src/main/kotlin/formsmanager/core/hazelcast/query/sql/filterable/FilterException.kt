package formsmanager.core.hazelcast.query.sql.filterable

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Filter Exception used when there is a Filterable Exception.
 * Typically thrown when there is SqlPredicate parsing exception or SqlPredicate Rule Validation exceptions.
 */
class FilterException(message: String, val filter: String, exception: Throwable? = null): RuntimeException(message, exception){
    fun toFilterError(): FilterError {
        return FilterError(message
                ?: "Filter Error", filter)
    }
}

/**
 * Error Message for HTTP when there is a Filterable exception.
 * @param message The error message.
 * @param filter The filter value that was provided.
 */
@Schema
data class FilterError(
        val message: String,
        val filter: String
)