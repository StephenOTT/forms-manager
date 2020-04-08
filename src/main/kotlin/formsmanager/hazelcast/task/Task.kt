package formsmanager.hazelcast.task

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import formsmanager.hazelcast.context.InjectAware
import java.util.concurrent.Callable

@InjectAware
abstract class Task<R> : Callable<R>, HazelcastInstanceAware{

    @Transient
    private lateinit var hazelcast: HazelcastInstance

    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        hazelcast = hazelcastInstance
    }

}