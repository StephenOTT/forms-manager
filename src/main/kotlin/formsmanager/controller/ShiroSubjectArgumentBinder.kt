package formsmanager.controller

import formsmanager.security.ShiroAuthenicationJwtClaimsSetAdapter
import io.micronaut.context.annotation.Requires
import io.micronaut.core.bind.ArgumentBinder.BindingResult
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder
import io.micronaut.http.filter.OncePerRequestHttpServerFilter
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.filters.SecurityFilter
import org.apache.shiro.subject.Subject
import java.util.*
import javax.inject.Singleton

@Singleton
@Requires(classes=[Subject::class])
class ShiroSubjectArgumentBinder : TypedRequestArgumentBinder<Subject> {

    override fun argumentType(): Argument<Subject> {
        return Argument.of(Subject::class.java)
    }

    override fun bind(context: ArgumentConversionContext<Subject>, source: HttpRequest<*>): BindingResult<Subject> {
        return (if (source.attributes.contains(OncePerRequestHttpServerFilter.getKey(SecurityFilter::class.java))) {
            // based on the design of the AuthenticationArgumentBinder
            val auth = source.getUserPrincipal(ShiroAuthenicationJwtClaimsSetAdapter::class.java)

            if (auth.isPresent){
                // Get the Subject from the Authentication's attributes
               val authSubject: Subject = kotlin.runCatching {
                    auth.get().shrioSubject
                }.getOrElse {
                   throw it //@TODO review for better error message handling
               }

                BindingResult { Optional.of(authSubject) }

            } else {
                BindingResult.EMPTY
            }

        } else {
            BindingResult.UNSATISFIED
        }) as BindingResult<Subject>
    }
}