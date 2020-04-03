package formsmanager.hazelcast

interface CrudableObject<I : Any> {
    val id: I
    var ol: Long

    fun toEntity(): MapStoreItemWrapperEntity<*>
}