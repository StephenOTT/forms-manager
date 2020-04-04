package formsmanager.hazelcast.queue

import com.hazelcast.collection.IQueue
import formsmanager.hazelcast.topic.StandardMessageBusManager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class QueueWorker(
        val queue: IQueue<*>,
        val qName: String,
        val taskType: KClass<*>,
        val mb: StandardMessageBusManager
){
    init {
        start().observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe() // @TODO add subject observer to stop it.
    }

    companion object{
        private val log = LoggerFactory.getLogger(QueueWorker::class.java)
    }

    private fun start(): Observable<*> {
        return Observable.fromCallable {

        }
//        return Observable.fromCallable {
//            queue.take()
//        }.doOnSubscribe {
//            log.ifDebugEnabled { "Starting take() for $qName for ${taskType.qualifiedName}" }
//        }.doOnNext {
//            log.ifDebugEnabled { "Task taken from queue $qName for ${taskType.qualifiedName}: $it" }
//            println("GOt a Object!")
//            println(it::class.qualifiedName)
//
//            mb.publish("form-submission-validation"){
//                MessageWrapper(message = (it as TaskWrapper<FormSubmission>))
//            }
//        }.repeat()
    }
}