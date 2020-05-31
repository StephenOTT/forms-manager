package formsmanager.camunda.engine.expression

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import org.camunda.bpm.engine.impl.el.ExpressionManager
import javax.inject.Named
import javax.inject.Singleton

@Factory
class ExpressionManagerFactory {

    @Singleton
    @Primary
    @Named("micronaut-context")
    fun expressionManager(appCtx: ApplicationContext): ExpressionManager{
        return MicronautExpressionManager(appCtx)
    }

}