package formsmanager.hazelcast.task

import com.hazelcast.core.IExecutorService
import formsmanager.hazelcast.HazelcastJetManager
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
class TaskManager(
        private val hazelcastJetManager: HazelcastJetManager
){
    fun create(serviceName: String): IExecutorService {
        return hazelcastJetManager.defaultInstance.hazelcastInstance.getExecutorService(serviceName)
    }

    fun shutdown(serviceName: String){
        hazelcastJetManager.defaultInstance.hazelcastInstance.getExecutorService(serviceName).shutdown()
    }

    /**
     * R = Response object of task
     */
    fun <R> submit(serviceName: String, task: Task<R>): Single<R>{
        val service = hazelcastJetManager.defaultInstance.hazelcastInstance.getExecutorService(serviceName)
        return Single.fromFuture(service.submit(task))
    }
}
