package formsmanager.core.hazelcast.serialization

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Used as a class type wrapper for Jackson Smile serialization.
 * This is the primary wrapper used for wrapping all objects that got into a DB used typically in a MapStore.
 * @param clazz The full class name
 * @param data Any object that can be serialized by Jackson.
 *      This field implements polymorphic serialization/deserialization.
 *      The field uses the `clazz` property in the DataWrapper to determine the class to deserialize into.
 */
data class DataWrapper(
        val clazz: String,

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "clazz")
        val data: Any?
)