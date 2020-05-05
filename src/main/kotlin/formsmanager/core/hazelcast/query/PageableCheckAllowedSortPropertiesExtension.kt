package formsmanager.core.hazelcast.query

import io.micronaut.data.model.Pageable

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