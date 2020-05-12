package formsmanager.core.hazelcast.query.sql.validator

import com.hazelcast.query.Predicate
import com.hazelcast.query.impl.predicates.SqlPredicate
import formsmanager.core.hazelcast.query.sql.validator.SqlPredicateRules
import kotlin.reflect.KClass

/**
 * Provides SqlPredicate validation based on additional "business rules"
 * Typical usage is to limit what Predicates and attributes can be used.
 * Provides a `attributeCustomRules` argument to provide custom rules.
 */
fun SqlPredicate.checkPredicateRules(acceptedAttributes: List<String>? = null,
                                     acceptedPredicates: List<KClass<out Predicate<*, *>>>? = null,
                                     prohibitAttributes: List<String>? = null,
                                     prohibitPredicates: List<KClass<out Predicate<*, *>>>? = null,
                                     attributeCustomRules: List<SqlPredicateRules.AttributeRule>? = null
): SqlPredicate {

    SqlPredicateRules.processSqlStructure(this)
    SqlPredicateRules.process(
            this.predicate,
            acceptedAttributes,
            acceptedPredicates,
            prohibitAttributes,
            prohibitPredicates,
            attributeCustomRules
    )
    return this
}