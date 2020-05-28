package formsmanager.camunda.engine.session

import com.hazelcast.transaction.TransactionContext
import org.camunda.bpm.engine.impl.cfg.TransactionListener
import org.camunda.bpm.engine.impl.cfg.TransactionState
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.Session


data class HazelcastTransactionSession(
        val transactionContext: TransactionContext
) : Session {

    init {
        transactionContext.beginTransaction()

        val transactionCommitListener = TransactionListener {
            transactionContext.commitTransaction()
        }

        val transactionRollbackListener = TransactionListener {
            transactionContext.rollbackTransaction()
        }

        val transactionContext = Context.getCommandContext().transactionContext
        transactionContext.addTransactionListener(TransactionState.COMMITTED, transactionCommitListener)
        transactionContext.addTransactionListener(TransactionState.ROLLED_BACK, transactionRollbackListener)

    }

    /**
     * Does nothing
     */
    override fun flush() {
    }

    /**
     * Does nothing
     */
    override fun close() {
    }
}

