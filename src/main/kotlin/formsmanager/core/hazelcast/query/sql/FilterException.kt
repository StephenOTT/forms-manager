package formsmanager.core.hazelcast.query.sql

class FilterException(message: String, val filter: String, exception: Throwable? = null): RuntimeException(message, exception){
    fun toFilterError(): FilterError{
        return FilterError(message ?: "Filter Error", filter)
    }
}

data class FilterError(
        val message: String,
        val filter: String
)