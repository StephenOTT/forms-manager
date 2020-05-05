package formsmanager.core.hazelcast.query

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Returns the BeanDescription for the provided pojo.
 */
inline fun <reified T> ObjectMapper.beanDescription(): BeanDescription {
    return this.serializationConfig.introspect(
            this.typeFactory.constructType(T::class.java)
    )
}