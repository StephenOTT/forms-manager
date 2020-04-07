package formsmanager.hazelcast.task

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import java.util.concurrent.Callable

abstract class Task<R> : Callable<R>, HazelcastInstanceAware{

    @Transient
    private lateinit var hazelcast: HazelcastInstance

    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        hazelcast = hazelcastInstance
    }

}