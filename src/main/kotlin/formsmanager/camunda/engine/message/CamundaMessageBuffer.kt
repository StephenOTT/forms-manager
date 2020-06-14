package formsmanager.camunda.engine.message

import com.hazelcast.map.IMap
import io.reactivex.Single
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CamundaMessageBuffer(
        @param:Named(BUFFER_NAME) private val messages: IMap<String, MessageWrapper>
) {

    companion object {
        const val BUFFER_NAME = "camunda-message-buffer"
    }

    fun insert(messageRequest: MessageRequest): Single<MessageWrapper> {
        return Single.fromCallable {
            //@ADD a waiting state so can submit job at this level so can get a future back...
            val wrapper = MessageWrapper(messageRequest)
            // @TODO add logic for overflow handling
            messages[wrapper.id.toMapKey()] = wrapper
            wrapper
        }
    }
}