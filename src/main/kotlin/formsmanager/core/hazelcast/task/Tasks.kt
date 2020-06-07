package formsmanager.core.hazelcast.task

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import formsmanager.core.hazelcast.context.InjectAware
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable

/**
 * Tasks for execution that have a return (could also be a Unit return).
 * Based on Callable
 */
@InjectAware
abstract class Task<R> : Callable<R>, HazelcastInstanceAware{

    companion object {
        internal val LOG = LoggerFactory.getLogger(Task::class.java)
    }

    @Transient @JsonIgnore
    lateinit var hazelcast: HazelcastInstance

    @JsonIgnore
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

    companion object {
        internal val LOG = LoggerFactory.getLogger(TaskWithoutReturn::class.java)
    }

    @Transient @JsonIgnore
    lateinit var hazelcast: HazelcastInstance

    @JsonIgnore
    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        hazelcast = hazelcastInstance
    }

}