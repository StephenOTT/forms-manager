package formsmanager.camunda.engine.services

import formsmanager.camunda.events.CamundaReactiveEvents
import formsmanager.camunda.events.ExternalTaskUnlocked
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.camunda.bpm.engine.impl.ExternalTaskServiceImpl
import org.camunda.bpm.engine.impl.cfg.TransactionState
import org.camunda.bpm.engine.impl.context.Context
import javax.inject.Singleton

@Singleton
class CustomExternalTaskServiceImpl(
        private val events: CamundaReactiveEvents
) : ExternalTaskServiceImpl() {

    private val externalTasksEvents = events.externalTaskEvents

    override fun unlock(externalTaskId: String) {
        return kotlin.runCatching {
            super.unlock(externalTaskId)
        }.onSuccess {
            Context.getCommandContext().transactionContext
                    .addTransactionListener(TransactionState.COMMITTED) {
                        Completable.fromAction {
                            externalTasksEvents.onNext(
                                    ExternalTaskUnlocked(externalTaskId)
                            )
                        }.subscribeOn(Schedulers.io()).subscribe()
                    }
        }.getOrThrow()
    }
}