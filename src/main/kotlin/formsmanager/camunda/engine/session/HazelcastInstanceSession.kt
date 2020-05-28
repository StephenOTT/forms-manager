package formsmanager.camunda.engine.session

import com.hazelcast.core.HazelcastInstance
import org.camunda.bpm.engine.impl.interceptor.Session

data class HazelcastInstanceSession(
        val hazelcastInstance: HazelcastInstance
) : Session {

    /**
     * Does nothing
     */
    override fun flush() {
    }

    /**
     * Does nothing
     */
    override fun close() {
    }
}