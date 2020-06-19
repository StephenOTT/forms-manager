package formsmanager.camunda.messagebuffer.processor

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hazelcast.scheduledexecutor.IScheduledExecutorService
import formsmanager.camunda.messagebuffer.MessageRequest
import formsmanager.camunda.messagebuffer.MessageWrapper
import formsmanager.camunda.messagebuffer.repository.MessageBufferHazelcastRepository
import formsmanager.core.hazelcast.context.InjectAware
import formsmanager.core.hazelcast.task.TaskWithoutReturn
import io.micronaut.context.annotation.Parameter
import org.camunda.bpm.engine.MismatchingMessageCorrelationException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType
import org.springframework.util.backoff.BackOffExecution
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Processes MessageWrapper objects / Messages for correlation.
 * If messages cannot be correlated on first try, then messageWrapper is submitted
 * into IScheduledExecutorService / camunda-message-process-retry service.
 *
 * @param messageId the ID of the MessageWrapper which is used to retrieve from the camunda-message-buffer map.
 * @param exponentialBackoffConfiguration configuration for the exponential backoff logic if correlation fails.
 */
@InjectAware
class MessageProcessorTask(
        @Parameter val messageId: String,
        @Parameter val exponentialBackoffConfiguration: ExponentialBackoffConfiguration = ExponentialBackoffConfiguration()
) : TaskWithoutReturn() {

    //@TODO import the single springboot package for backoff
    @JsonIgnore
    private val retryDelay: BackOffExecution = exponentialBackoffConfiguration.toExponentialBackOff().start().apply { nextBackOff() }

    @Transient
    @Inject
    @JsonIgnore
    lateinit var processEngine: ProcessEngine

    @delegate:Transient
    private val messageProcessorRetryService: IScheduledExecutorService by lazy {
        hazelcast.getScheduledExecutorService(MessageBufferProcessorFactory.MESSAGE_BUFFER_PROCESSOR_RETRY)
    }

    @Transient
    @Inject
    @JsonIgnore
    lateinit var messages: MessageBufferHazelcastRepository
    //    @field:Named(MessageBufferHazelcastRepository.MAP_NAME)
//    lateinit var messages: IMap<String, MessageWrapper>

    override fun run() {
//        val message: MessageWrapper? = messages[messageId]
        val message: MessageWrapper? = messages.get(messageId).blockingGet()
        //@TODO add better error handling
        requireNotNull(message)

        if (message.state != MessageWrapper.State.CORRELATING) {
            throw IllegalStateException("Message ${message.id} is not in correlating state.")
        }

        val builder = configureCorrelationBuilder(message.message)

        val attemptDate = Instant.now()

        val result: List<MessageCorrelationResult> = if (message.message.all) {
            builder.correlateAllWithResult()
        } else {
            kotlin.runCatching {
                listOf(builder.correlateWithResult())
            }.getOrElse {
                if (it is MismatchingMessageCorrelationException){
                    listOf()
                } else {
                    throw it
                }
            }
        }

        if (result.isEmpty()) {
            val delay = retryDelay.nextBackOff()

            if (delay != BackOffExecution.STOP) {
                message.addAttempt(attemptDate)

                // Update the message in the
//                messages.replace(message.id.toMapKey(), message)
                messages.update(message.id.toMapKey(), message)

                println("Retrying with delay: ${delay}")
                messageProcessorRetryService.schedule<Unit>(MessageProcessorTask(messageId, exponentialBackoffConfiguration.copy(initialInterval = delay)), delay, TimeUnit.MILLISECONDS)
            } else {
                println("Expiring")
                message.expire()
            }
        } else {
            val correlationData = kotlin.runCatching {
                result.map {
                    val execution: String? = if (it.resultType == MessageCorrelationResultType.Execution) it.execution.id else null
                    MessageWrapper.CorrelationResult(attemptDate,
                            it.resultType.name,
                            it.processInstance.processInstanceId,
                            execution)
                }
            }.getOrThrow()

            kotlin.runCatching {
                message.addCorrelation(attemptDate, correlationData)
                message.correlated()
//                messages.replace(message.id.toMapKey(), message)
                messages.update(message.id.toMapKey(), message)
            }.onFailure {
                throw IllegalStateException("Unable to update message correlation data for ${message.id}. $message", it)
            }
        }
    }

    /**
     * Convert the MessageRequest into a configured MessageCorrelationBuilder
     */
    private fun configureCorrelationBuilder(message: MessageRequest): MessageCorrelationBuilder {
        val builder = processEngine.runtimeService.createMessageCorrelation(message.name)

        message.businessKey?.let {
            builder.processInstanceBusinessKey(it)
        }

        message.tenantId?.let {
            builder.tenantId(it)
        }

        if (message.withoutTenantId){
            builder.withoutTenantId()
        }

        message.processInstanceId?.let {
            builder.processInstanceId(it)
        }

        message.processDefinitionId?.let {
            builder.processDefinitionId(it)
        }

        message.correlationKeys?.let {
            builder.processInstanceVariablesEqual(it)
        }

        message.localCorrelationKeys?.let {
            builder.localVariablesEqual(it)
        }

        message.processVariables?.let {
            builder.setVariables(it)
        }

        message.processVariablesLocal?.let {
            builder.setVariablesLocal(it)
        }

        if (message.startMessagesOnly){
            builder.startMessageOnly()
        }

        return builder
    }
}