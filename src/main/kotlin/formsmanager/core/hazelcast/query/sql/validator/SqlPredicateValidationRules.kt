package formsmanager.core.hazelcast.query.sql.validator

import com.hazelcast.query.Predicate
import formsmanager.core.hazelcast.query.sql.validator.SqlPredicateRules
import kotlin.reflect.KClass

/**
 * Wrapper class for SqlPredicate Validation Rules.
 * This is primary used as a data transport for storage of sets of rules.
 */
data class SqlPredicateValidationRules(
        val acceptedAttributes: List<String>? = null,
        val acceptedPredicates: List<KClass<out Predicate<*, *>>>? = null,
        val prohibitAttributes: List<String>? = null,
        val prohibitPredicates: List<KClass<out Predicate<*, *>>>? = null,
        val attributeCustomRules: List<SqlPredicateRules.AttributeRule>? = null
)