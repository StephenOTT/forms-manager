package formsmanager.core.hazelcast.query.sql

import com.hazelcast.query.Predicate
import kotlin.reflect.KClass

/**
 * Wrapper class for SqlPredicate Validation Rules
 */
data class SqlPredicateValidationRules(
        val acceptedAttributes: List<String>? = null,
        val acceptedPredicates: List<KClass<out Predicate<*, *>>>? = null,
        val prohibitAttributes: List<String>? = null,
        val prohibitPredicates: List<KClass<out Predicate<*, *>>>? = null,
        val attributeCustomRules: List<SqlPredicateRules.AttributeRule>? = null
)