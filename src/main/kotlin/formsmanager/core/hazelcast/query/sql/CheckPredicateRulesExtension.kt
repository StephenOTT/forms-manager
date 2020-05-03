package formsmanager.core.hazelcast.query.sql

import com.hazelcast.query.Predicate
import com.hazelcast.query.impl.predicates.SqlPredicate
import kotlin.reflect.KClass

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