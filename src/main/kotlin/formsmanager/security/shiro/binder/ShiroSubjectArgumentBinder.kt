package formsmanager.security.shiro.binder

import formsmanager.ifDebugEnabled
import formsmanager.security.ShiroMicronautSecurityService
import io.micronaut.context.annotation.Requires
import io.micronaut.core.bind.ArgumentBinder.BindingResult
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder
import io.micronaut.http.filter.OncePerRequestHttpServerFilter
import io.micronaut.security.filters.SecurityFilter
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
@Requires(classes = [Subject::class])
class ShiroSubjectArgumentBinder(
        private val shiroMicronautSecurityService: ShiroMicronautSecurityService
) : TypedRequestArgumentBinder<Subject> {

    private val log = LoggerFactory.getLogger(ShiroSubjectArgumentBinder::class.java)

    override fun argumentType(): Argument<Subject> {
        return Argument.of(Subject::class.java)
    }

    override fun bind(context: ArgumentConversionContext<Subject>, source: HttpRequest<*>): BindingResult<Subject> {
        return (if (source.attributes.contains(OncePerRequestHttpServerFilter.getKey(SecurityFilter::class.java))) {
            log.ifDebugEnabled { "Shiro Subject TypedRequestArgumentBinder has started." }

            BindingResult {Optional.of(shiroMicronautSecurityService.getSubjectFromHttpRequest(source))}

        } else {
            BindingResult.UNSATISFIED
        }) as BindingResult<Subject>
    }
}