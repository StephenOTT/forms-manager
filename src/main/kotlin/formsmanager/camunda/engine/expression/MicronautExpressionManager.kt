package formsmanager.camunda.engine.expression

import io.micronaut.context.ApplicationContext
import org.camunda.bpm.engine.impl.el.ExpressionManager
import org.camunda.bpm.engine.impl.el.ReadOnlyMapELResolver
import org.camunda.bpm.engine.impl.el.VariableContextElResolver

import org.camunda.bpm.engine.impl.el.VariableScopeElResolver
import org.camunda.bpm.engine.impl.javax.el.*


class MicronautExpressionManager(
        private val applicationContext: ApplicationContext
) : ExpressionManager() {

    // @TODO add support for the custom bean definitions in the expression manager
    // Just need to use the constructor for ExpressionManager

    override fun createElResolver(): ELResolver {
        val compositeElResolver = CompositeELResolver()
        compositeElResolver.add(VariableScopeElResolver())
        compositeElResolver.add(VariableContextElResolver())
        if (beans != null) {
            // Only expose limited set of beans in expressions
            compositeElResolver.add(ReadOnlyMapELResolver(beans))
        } else {
            // Expose full application-context in expressions
            compositeElResolver.add(ApplicationContextElResolver(applicationContext))
        }
        compositeElResolver.add(ArrayELResolver())
        compositeElResolver.add(ListELResolver())
        compositeElResolver.add(MapELResolver())
        compositeElResolver.add(BeanELResolver())
        return compositeElResolver
    }

}