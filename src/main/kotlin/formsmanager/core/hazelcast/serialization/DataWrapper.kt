package formsmanager.core.hazelcast.serialization

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Used as a class type wrapper for Jackson Smile serialization
 */
data class DataWrapper(
        val clazz: String,

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "clazz")
        val data: Any?
)