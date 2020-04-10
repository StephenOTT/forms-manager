package formsmanager.hazelcast.task

import com.hazelcast.core.IExecutorService
import formsmanager.hazelcast.HazelcastJetManager
import formsmanager.ifDebugEnabled
import io.reactivex.Single
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class TaskManager(
        private val hazelcastJetManager: HazelcastJetManager
) {

    companion object {
        private val log = LoggerFactory.getLogger(TaskManager::class.java)
    }

    fun create(serviceName: String): IExecutorService {
        return hazelcastJetManager.defaultInstance.hazelcastInstance.getExecutorService(serviceName)
    }

    fun shutdown(serviceName: String) {
        hazelcastJetManager.defaultInstance.hazelcastInstance.getExecutorService(serviceName).shutdown()
    }

    /**
     * R = Response object of task
     */
    fun <R> submit(serviceName: String, task: Task<R>): Single<R> {
        log.ifDebugEnabled { "Sending a Task with $serviceName" }
        val service = hazelcastJetManager.defaultInstance.hazelcastInstance.getExecutorService(serviceName)
        return Single.fromFuture(service.submit(task))
    }
}
