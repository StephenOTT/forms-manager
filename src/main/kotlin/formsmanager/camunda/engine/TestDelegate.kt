package formsmanager.camunda.engine

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import javax.inject.Named
import javax.inject.Singleton


@Singleton
@Named("MyDel")
class TestDelegate : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        println("!!!HAPPY!!!")
    }
}