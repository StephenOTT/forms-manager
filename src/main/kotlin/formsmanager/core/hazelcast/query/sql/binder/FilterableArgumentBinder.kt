package formsmanager.core.hazelcast.query.sql.binder

import formsmanager.core.hazelcast.query.sql.FilterException
import formsmanager.core.hazelcast.query.sql.Filterable
import io.micronaut.context.annotation.Requires
import io.micronaut.core.bind.ArgumentBinder.BindingResult
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder
import java.util.*
import javax.inject.Singleton

@Singleton
@Requires(classes = [Filterable::class])
class FilterableArgumentBinder : TypedRequestArgumentBinder<Filterable> {

    override fun argumentType(): Argument<Filterable> {
        return Argument.of(Filterable::class.java)
    }

    override fun bind(context: ArgumentConversionContext<Filterable>, source: HttpRequest<*>): BindingResult<Filterable> {
        val filterString: Optional<String> = source.parameters.getFirst(Filterable.FILTER_PARAMETER, String::class.java)

        // if filter parameter is present in http request
        return if (filterString.isPresent) {
            kotlin.runCatching {
                BindingResult {
                    Optional.of(
                            Filterable.from(filterString.get())
                    )
                }
            }.getOrElse {
                throw FilterException("Invalid filter query structure.", it)
            }

            // If the filter parameter was not present in http request
        } else {
            BindingResult {
                Optional.of(
                        Filterable.unfiltered()
                )
            }
        }
    }
}