package formsmanager.hazelcast.annotation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class QueueConsumer(
        val name: String
) {}