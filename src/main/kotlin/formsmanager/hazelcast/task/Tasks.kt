package formsmanager.hazelcast.task

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import formsmanager.hazelcast.context.InjectAware
import java.util.concurrent.Callable

/**
 * Tasks for execution that have a return (could also be a Unit return).
 * Based on Callable
 */
@InjectAware
abstract class Task<R> : Callable<R>, HazelcastInstanceAware{

    @Transient
    private lateinit var hazelcast: HazelcastInstance

    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        hazelcast = hazelcastInstance
    }

}

/**
 * Tasks that do not require any form of return
 * Based on Runnable
 */
@InjectAware
abstract class TaskWithoutReturn : Runnable, HazelcastInstanceAware{

    @Transient
    private lateinit var hazelcast: HazelcastInstance

    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        hazelcast = hazelcastInstance
    }

}