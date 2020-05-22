package formsmanager.camunda.externaltask.handler.http

import formsmanager.camunda.events.CamundaReactiveEvents
import formsmanager.camunda.events.ExternalTaskCreated
import formsmanager.camunda.events.ExternalTaskUnlocked
import formsmanager.camunda.externaltask.controller.FetchAndLockResponse
import formsmanager.camunda.externaltask.controller.HttpRequestLifecycle
import formsmanager.camunda.externaltask.subscription.ExternalTaskSubscription
import formsmanager.camunda.externaltask.subscription.HttpExternalTaskSubscription
import formsmanager.camunda.externaltask.subscription.TopicDefinition
import formsmanager.core.ifDebugEnabled
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.externaltask.ExternalTask
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
class HttpExternalTaskReactiveHandler(
        private val events: CamundaReactiveEvents,
        private val engine: ProcessEngine,
        private val subscriptions: ConcurrentLinkedQueue<ExternalTaskSubscription>,
        private val handlerConfiguration: ExternalTaskHandlerConfiguration,
        private val cycle: HttpRequestLifecycle
) {

    private val log = LoggerFactory.getLogger(HttpExternalTaskReactiveHandler::class.java)

    // Future optimization to use: https://forum.camunda.org/t/lock-external-task-by-id/5620/4

    /**
     * Handler for processing task Created
     * Backpressure to 10,000 items
     */
    val externalTaskCreatedEventHandler = events.externalTaskEvents
            .subscribeOn(Schedulers.io())
            .toFlowable(BackpressureStrategy.LATEST)
            .onBackpressureBuffer(handlerConfiguration.createdEventBuffer)
            .filter { it is ExternalTaskCreated }
            .doOnNext {
                log.ifDebugEnabled { "Task Created Event Handler: Create Event received for taskId: ${it.taskId}" }
            }.forEach { taskEvent ->
                val task = engine.externalTaskService
                        .createExternalTaskQuery()
                        .externalTaskId(taskEvent.taskId)
                        .singleResult()

                if (task == null) {
                    log.error("Unable to process ExternalTaskCreated event for task ${taskEvent.taskId}: Could not find taskId in Camunda DB.")

                } else {
                    subscriptions.firstOrNull { sub ->
                        if (!sub.requestFuture.isDone) {
                            // Check each topic the subscription has
                            sub.topics.any { td ->
                                checkExternalTaskMatchesTopicDefinition(task, td)
                            }
                        } else {
                            // The subscription was not active/ channel was closed
                            // @TODO consider adding a removal from the list/queue trigger
                            false
                        }
                    }?.run {
                        val lockedTask = fetchAndLock(this)

                        if (lockedTask.isNotEmpty()) {
                            Completable.fromAction {
                                provideResponse(FetchAndLockResponse(lockedTask))
                            }.andThen {
                                subscriptions.remove(this)
                            }.subscribeOn(Schedulers.io()).subscribe()
                        }
                    }
                }
            }

    val externalTaskUnlockedEventHandler = events.externalTaskEvents
            .subscribeOn(Schedulers.io())
            .toFlowable(BackpressureStrategy.LATEST)
            .onBackpressureBuffer(handlerConfiguration.unlockedEventBuffer)
            .filter { it is ExternalTaskUnlocked }
            .doOnNext {
                log.ifDebugEnabled { "Task Created Event Handler: Create Event received for taskId: ${(it as ExternalTaskUnlocked).taskId}" }
            }
            .forEach { taskEvent ->
                val task = engine.externalTaskService
                        .createExternalTaskQuery()
                        .externalTaskId(taskEvent.taskId)
                        .singleResult()

                if (task == null) {
                    log.error("Unable to process ExternalTaskCreated event for task ${taskEvent.taskId}: Could not find taskId in Camunda DB.")

                } else {
                    subscriptions.firstOrNull { sub ->
                        if (!sub.requestFuture.isDone) {
                            // Check each topic the subscription has
                            sub.topics.any { td ->
                                checkExternalTaskMatchesTopicDefinition(task, td)
                            }
                        } else {
                            // The subscription was not active/ channel was closed
                            // @TODO consider adding a removal from the list/queue trigger
                            false
                        }
                    }?.run {
                        val lockedTask = fetchAndLock(this)

                        if (lockedTask.isNotEmpty()) {
                            Completable.fromAction {
                                provideResponse(FetchAndLockResponse(lockedTask))
                            }.andThen {
                                subscriptions.remove(this)
                            }.subscribeOn(Schedulers.io()).subscribe()
                        }
                    }
                }
            }

    /**
     * Removes ExternalTaskSubscriptions from the subscriptions concurrent list (queue) based on the Netty
     * Request's Channel being closed, and thus connection is no longer active.
     */
    val externalTaskLongPollRequestCleaner = Observable
            .timer(handlerConfiguration.cleanClosedConnectionsCycle, TimeUnit.SECONDS)
            .repeat()
            .subscribeOn(Schedulers.io())
            .subscribe {
                //@TODO consider moving the below to a parallel flowable for better performance?
                subscriptions.removeIf {
                    // HTTP request does not tell you when it has completed.
                    // So some sort of other future is required
                    if (it is HttpExternalTaskSubscription) {
                        if (it.requestFuture.isDone) {
                            log.ifDebugEnabled { "Long-poll cleaning: Dead Channel detected: removing subscription for a worker ${it.workerId}" }
                            true
                        } else {
                            log.ifDebugEnabled { "Long-poll cleaning: channel is still active for a worker ${it.workerId}" }
                            false
                        }

                    } else {
                        // not a HttpBasedExternalTaskSubscription so dont need to remove it.
                        log.ifDebugEnabled { "Long-poll cleaning: No Http based requests found in subscriptions for worker ${it.workerId}" }
                        false
                    }
                }
                log.ifDebugEnabled { "Long-poll cleaning: HttpRequest cleaning completed." }
            }

    /**
     * Start new Subscription flow
     * This could be used with HttpRequests, Web Sockets, message bus, etc.
     *
     */
    fun newSubscription(subscription: ExternalTaskSubscription): Observable<FetchAndLockResponse> {
        //lateinit the request future before doing
        subscription.requestFuture = cycle.requestFuture

        val tasks = fetchAndLock(subscription)

        // If there were tasks that match the criteria then provide the response immediately
        if (tasks.isNotEmpty()) {
            subscription.provideResponse(FetchAndLockResponse(tasks))

        } else {
            // If no tasks were returned then add the subscription to the subscription queue/list
            subscriptions.add(subscription)
        }
        return subscription.observable()
    }

    /**
     * Fetch and Lock based on a ExternalTaskSubscription
     * Designed to be used only by the handler within a existing reactive controlled thread.
     */
    private fun fetchAndLock(subscription: ExternalTaskSubscription): List<LockedExternalTask> {
        val sub = engine.externalTaskService.fetchAndLock(subscription.maxTasks, subscription.workerId, subscription.usePriority)

        subscription.topics.forEach { def ->
            val topic = sub.topic(def.topicName, def.lockDuration)

            def.businessKey?.let {
                topic.businessKey(it)
            }

            if (def.deserializeValues) {
                topic.enableCustomObjectDeserialization()
            }

            if (def.localVariables) {
                topic.localVariables()
            }

            def.processDefinitionIdIn?.let {
                topic.processDefinitionIdIn(*it.toTypedArray())
            }

            def.processDefinitionKeyIn?.let {
                topic.processDefinitionKeyIn(*it.toTypedArray())
            }

            def.processDefinitionVersionTag?.let {
                topic.processDefinitionVersionTag(it)
            }

            def.tenantIdIn?.let {
                topic.tenantIdIn(*it.toTypedArray())
            }

            def.withoutTenantId?.let {
                if (it) {
                    topic.withoutTenantId()
                }
            }

            def.processVariables?.let {
                topic.processInstanceVariableEquals(it)
            }

            def.variables?.let {
                topic.variables(*it.toTypedArray())
            }

        }
        return sub.execute()
    }

    /**
     * Validation logic for comparing a Topic Subscription with a External Task
     * @param externalTaskExecutionId the Execution ID of the external task: Used to retrieve the variables for comparison
     */
    private fun checkExternalTaskMatchesTopicDefinition(externalTask: ExternalTask, topicDefinition: TopicDefinition): Boolean {
        val topic = topicDefinition.topicName == externalTask.topicName
        // Optimization: No need to check further if topic does not match.
        return if (topic) {

            val withoutTenant = topicDefinition.withoutTenantId?.let {
                externalTask.tenantId == null
            } ?: true

            val tenant = topicDefinition.tenantIdIn?.let {
                externalTask.tenantId in it
            } ?: true

            val businessKey = topicDefinition.businessKey?.let {
                externalTask.businessKey == it
            } ?: true

            val defIds = topicDefinition.processDefinitionIdIn?.let {
                externalTask.processDefinitionId in it
            } ?: true

            val defKeys = topicDefinition.processDefinitionKeyIn?.let {
                externalTask.processDefinitionKey in it
            } ?: true

            val defVTag = topicDefinition.processDefinitionVersionTag?.let {
                externalTask.processDefinitionVersionTag == it
            } ?: true

            val initialResult = withoutTenant && tenant && businessKey && defIds && defKeys && defVTag

            // Optimization: No need to look up variables in DB if the rest of the triggers do not match.
            if (initialResult && topicDefinition.processVariables != null) {
                val variables = engine.runtimeService.getVariables(externalTask.executionId, topicDefinition.processVariables.keys)
                topicDefinition.processVariables == variables
            } else {
                // If there were no process variables query in the subscription
                initialResult
            }

        } else {
            // If topic does not match
            false
        }
    }
}