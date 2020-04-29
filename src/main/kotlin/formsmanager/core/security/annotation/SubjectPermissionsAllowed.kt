package formsmanager.core.security.annotation

import io.micronaut.aop.Around
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Type
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.subject.Subject
import javax.inject.Singleton


/**
 * WIP not ready for use.
 * Current blocker is used of parameters to inject into the rules.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@Around
@Type(SubjectPermissionsAllowedInterceptor::class)
annotation class SubjectPermissionsAllowed(
        val value: Array<String>,
        val logical: Logical = Logical.AND
)

@Singleton
class SubjectPermissionsAllowedInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>) {
        val subject: Subject? = context.parameterValues.filterIsInstance<Subject>().singleOrNull()

        // If there is a subject argument then:
        if (subject != null) {
            val permissions = context.getAnnotation(SubjectPermissionsAllowed::class.java)!!.getRequiredValue(Array<String>::class.java)
            val logicalValue = context.getAnnotation(SubjectPermissionsAllowed::class.java)!!.getRequiredValue("logical", Logical::class.java)

                val resultSet = subject.isPermitted(*permissions)

                val result = when (logicalValue) {
                    Logical.AND -> {
                        resultSet.all { it }

                    }
                    Logical.OR -> {
                        resultSet.any { it }

                    }
                    else -> {
                        throw IllegalStateException("Unexpected Logical value in annotaiton configuration")
                    }
                }

            if (result) {
                // If all good then
                context.proceed()
            } else {
                throw AuthorizationException("Subject ${subject.principal} is not permitted for the requested action.")
            }
        } else {
            // If no subject and proceed
            context.proceed()
        }
    }
}