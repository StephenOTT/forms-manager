package formsmanager.core.hazelcast.annotation

import com.hazelcast.map.MapStore
import kotlin.reflect.KClass


//@TODO rebuild this to be a better usage
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MapStore(
        val value: KClass<out MapStore<*,*>>,
        val mapName: String
)