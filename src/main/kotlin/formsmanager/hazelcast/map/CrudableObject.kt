package formsmanager.hazelcast.map

interface CrudableObject<I : Any> {
    val id: I
    var ol: Long

    fun toEntity(): MapStoreItemWrapperEntity<*>
}