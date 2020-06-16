package formsmanager.camunda.hazelcast.session

import com.hazelcast.core.HazelcastInstance
import io.micronaut.context.annotation.Factory
import org.camunda.bpm.engine.impl.interceptor.Session
import org.camunda.bpm.engine.impl.interceptor.SessionFactory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class CamundaHazelcastSessionFactoryFactory {

    @Singleton
    @Named("hazelcast-transaction")
    fun hazelcastTransactionSessionFactory(hazelcastInstance: HazelcastInstance): SessionFactory {
        return object : SessionFactory {
            override fun getSessionType(): Class<*> {
                return HazelcastTransactionSession::class.java
            }

            override fun openSession(): Session {
                return HazelcastTransactionSession(
                        hazelcastInstance.newTransactionContext()
                )
            }
        }
    }

    @Singleton
    @Named("hazelcast-instance")
    fun hazelcastInstanceSessionFactory(hazelcastInstance: HazelcastInstance): SessionFactory {
        return object : SessionFactory {
            override fun getSessionType(): Class<*> {
                return HazelcastInstanceSession::class.java
            }

            override fun openSession(): Session {
                return HazelcastInstanceSession(hazelcastInstance)
            }
        }
    }
}