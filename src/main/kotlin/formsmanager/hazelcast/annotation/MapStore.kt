package formsmanager.hazelcast.annotation

import com.hazelcast.map.MapStore
import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MapStore(
        val value: KClass<out MapStore<*,*>>,
        val mapName: String
)