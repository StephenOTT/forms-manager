package formsmanager.camunda.messagebuffer.service

import formsmanager.camunda.messagebuffer.repository.MessageBufferHazelcastRepository
import formsmanager.camunda.messagebuffer.MessageRequest
import formsmanager.camunda.messagebuffer.MessageWrapper
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
class MessageBufferService(
        private val messageBufferRepository: MessageBufferHazelcastRepository
) {

    //@TODO refactor the fun name...
    /**
     * Insert MessageRequest into the message buffer
     */
    fun addToBuffer(messageRequest: MessageRequest): Single<MessageWrapper> {
        return Single.fromCallable {
            //@ADD a waiting state so can submit job at this level so can get a future back...
            MessageWrapper(messageRequest)
        }.flatMap {
            messageBufferRepository.create(it.id.toMapKey(), it)
        }
    }
}