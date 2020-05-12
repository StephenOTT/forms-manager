package formsmanager.core.hazelcast.query.sql.binder

import formsmanager.core.hazelcast.query.sql.filterable.FilterException
import formsmanager.core.hazelcast.query.sql.filterable.Filterable
import formsmanager.core.hazelcast.query.sql.validator.SqlPredicateRules
import formsmanager.core.hazelcast.query.sql.validator.SqlPredicateValidationRules
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.AnnotationValue
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

        val filterControlAnn = context.argument.findAnnotation(FilterableControl::class.java)


        // if filter parameter is present in http request
        return if (filterString.isPresent) {
            kotlin.runCatching {
                // Parse string into a Filterable (SqlPredicate)
                val filter = Filterable.from(filterString.get())

                if (filterControlAnn.isPresent) {
                    // If the FilterableControl annotation is present:
                    // Build Rules based on Annotation, and check the Predicate against the rules.
                    val rules = buildSqlPredicateValidationRule(filterControlAnn.get())
                    filter.checkPredicateRules(rules)
                }
                // Return the filter
                BindingResult { Optional.of(filter) }
            }.getOrElse {
                throw FilterException(it.message
                        ?: "Invalid filter query structure.", filterString.get(), it)
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

    private fun buildSqlPredicateValidationRule(annValue: AnnotationValue<FilterableControl>): SqlPredicateValidationRules {
        val prohibitedPropertiesAnnValue = annValue.stringValues("prohibitProperties").asList()
        val prohibitedTypesAnnValue = annValue.enumValues("prohibitTypes", SqlPredicateRules.SqlPredicates::class.java).asList()

        val allowedPropertiesAnnValue = annValue.stringValues("allowProperties").asList()
        val allowedTypesAnnValue = annValue.enumValues("allowTypes", SqlPredicateRules.SqlPredicates::class.java).asList()

        //booleanValue() is not used, because if boolean is false, the optional returns empty.
        val allowThisKeyword = annValue.getRequiredValue("allowThisKeyword", Boolean::class.java)
        val allowEntryKeyAttributeKeyword = annValue.getRequiredValue("allowEntryKeyAttributeKeyword", Boolean::class.java)
        val allowArrayAnyKeyword = annValue.getRequiredValue("allowArrayAnyKeyword", Boolean::class.java)
        val allowArrayItemNumberAccessor = annValue.getRequiredValue("allowArrayItemNumberAccessor", Boolean::class.java)

        val prohibitProperties =
                if (prohibitedPropertiesAnnValue.isEmpty()) {
                    null
                } else {
                    prohibitedPropertiesAnnValue
                }

        val allowedProperties =
                if (prohibitProperties == null && allowedPropertiesAnnValue.isNotEmpty()) {
                    allowedPropertiesAnnValue
                } else {
                    null
                }

        val prohibitTypes =
                if (prohibitedTypesAnnValue.isEmpty()) {
                    null
                } else {
                    prohibitedTypesAnnValue
                }

        val allowedTypes =
                if (prohibitTypes == null && allowedTypesAnnValue.isNotEmpty()) {
                    allowedTypesAnnValue
                } else {
                    null
                }

        val customRules = listOfNotNull(
                if (!allowThisKeyword) SqlPredicateRules.CommonRules.noThisKeyword else null,
                if (!allowEntryKeyAttributeKeyword) SqlPredicateRules.CommonRules.noEntryKeyKeyword else null,
                if (!allowArrayAnyKeyword) SqlPredicateRules.CommonRules.noArrayAnyAccessor else null,
                if (!allowArrayItemNumberAccessor) SqlPredicateRules.CommonRules.noArrayAccessors else null
        )

        return SqlPredicateValidationRules(
                acceptedAttributes = allowedProperties,
                acceptedPredicates = allowedTypes?.map { it.predicateClass() },
                prohibitAttributes = prohibitProperties,
                prohibitPredicates = prohibitTypes?.map { it.predicateClass() },
                attributeCustomRules = customRules

        )
    }
}