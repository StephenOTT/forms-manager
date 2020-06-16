package formsmanager.camunda.externaltask

import formsmanager.camunda.events.CamundaReactiveEvents
import formsmanager.camunda.events.ExternalTaskCreated
import formsmanager.camunda.events.ExternalTaskEvent
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.camunda.bpm.engine.impl.cfg.TransactionState
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager
import javax.inject.Inject

class CustomExternalTaskManager : ExternalTaskManager() {

    @Inject
    private lateinit var events: CamundaReactiveEvents

    /**
     * Uses Lazy injection because of how Managers use the Manager Factories
     * See: [formsmanager.camunda.engine.MicronautContextAwareGenericManagerReplacerFactory]
     */
    private val externalTaskEvents: PublishSubject<ExternalTaskEvent> by lazy {
        events.externalTaskEvents
    }

    override fun insert(externalTask: ExternalTaskEntity) {
        super.insert(externalTask)
        val taskId = externalTask.id
        Context.getCommandContext().transactionContext
                .addTransactionListener(TransactionState.COMMITTED) {
                    Completable.fromAction {
                        externalTaskEvents.onNext(
                                ExternalTaskCreated(taskId)
                        )
                    }.subscribeOn(Schedulers.io()).subscribe()
                }
    }
}