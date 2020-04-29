package formsmanager.core.hazelcast.query

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable

/**
 * Returns the BeanDescription for the provided pojo.
 */
inline fun <reified T> ObjectMapper.beanDescription(): BeanDescription {
    return this.serializationConfig.introspect(
            this.typeFactory.constructType(T::class.java)
    )
}


/**
 * Throws IllegalArgumentException if Sort properties contains sort properties
 */
fun Pageable.checkAllowedSortProperties(properties: List<String>) {
    if (this.sort.isSorted) {
        require(properties.containsAll(this.sort.orderBy.map { it.property })) {
            "Only allowed to sort by properties: $properties"
        }
    }
}