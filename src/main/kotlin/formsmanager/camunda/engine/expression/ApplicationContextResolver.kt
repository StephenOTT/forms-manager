package formsmanager.camunda.engine.expression

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.javax.el.ELContext
import org.camunda.bpm.engine.impl.javax.el.ELResolver
import java.beans.FeatureDescriptor

/**
 * Used by [MicronautExpressionManager] to inject EL resolver with Micronaut beans.
 */
class ApplicationContextElResolver(
        private val applicationContext: ApplicationContext) : ELResolver() {
    override fun getValue(context: ELContext, base: Any?, property: Any): Any? {
        return if (base == null) {
            // according to javadoc, can only be a String
            val key = property as String
            val bean = applicationContext.findBean(Any::class.java, Qualifiers.byName(key))
                    .orElse(null)
            if (bean != null) {
                context.isPropertyResolved = true
            }
            bean
        } else {
            null
        }
    }

    override fun isReadOnly(context: ELContext, base: Any, property: Any): Boolean {
        return true
    }

    override fun setValue(context: ELContext, base: Any?, property: Any, value: Any) {
        if (base == null) {
            val key = property as String
            val bean = applicationContext.findBean(Any::class.java, Qualifiers.byName(key))
                    .orElse(null)
            if (bean != null) {
                throw ProcessEngineException("Cannot set value of '${property}', it resolves to a bean defined in the Micronaut application-context.")
            }
        }
    }

    override fun getCommonPropertyType(context: ELContext, arg: Any): Class<*> {
        return Any::class.java
    }

    override fun getFeatureDescriptors(context: ELContext, arg: Any): Iterator<FeatureDescriptor>? {
        return null
    }

    override fun getType(context: ELContext, arg1: Any, arg2: Any): Class<*> {
        return Any::class.java
    }

}