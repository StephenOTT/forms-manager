package formsmanager.core.hazelcast.map

interface CrudableObjectId<T>: Comparable<T>{

    fun toMapKey(): String

}