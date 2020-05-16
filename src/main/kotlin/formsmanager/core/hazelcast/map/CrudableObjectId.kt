package formsmanager.core.hazelcast.map

import java.io.Serializable

interface CrudableObjectId<T>: Comparable<T>, Serializable{

    val value: Any

    fun toMapKey(): String

    fun asString(): String

    fun type(): String

}