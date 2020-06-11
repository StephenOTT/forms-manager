package formsmanager.core.hazelcast.map

interface OptimisticLocking{
    /**
     * Optimistic locking value
     */
    val ol: Long
}