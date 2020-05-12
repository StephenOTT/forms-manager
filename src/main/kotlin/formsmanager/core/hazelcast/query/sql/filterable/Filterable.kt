package formsmanager.core.hazelcast.query.sql.filterable

import com.fasterxml.jackson.annotation.JsonCreator
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import com.hazelcast.query.impl.predicates.SqlPredicate
import formsmanager.core.hazelcast.query.sql.validator.checkPredicateRules
import formsmanager.core.hazelcast.query.sql.validator.SqlPredicateValidationRules

interface Filterable {

    val sqlPredicate: SqlPredicate?
    val unfiltered: Boolean

    companion object {
        /**
         * Default filter parameter key.
         */
        const val FILTER_PARAMETER = "filter" //@TODO move to config

        /**
         * Primary creation point for generation of a Filterable from a string.
         * The rawString will be converted into a SqlPredicate.
         */
        @JsonCreator
        fun from(rawString: String): Filterable {
            return DefaultFilterable(SqlPredicate(rawString))
        }

        /**
         * Create a unfiltered Filterable.
         */
        fun unfiltered(): Filterable {
            return DefaultFilterable()
        }
    }

    /**
     * If the Filterable is a filtered (true) or Unfiltered(false).
     * Unfiltered typically occurs when the filter param in a HTTP request is not provided.
     */
    fun isFiltered(): Boolean{
        return !unfiltered
    }

    /**
     * Validates Predicate Rules for SqlPredicate processing
     * If is not filtered isFiltered == false, then rules are not processed
     * @exception IllegalArgumentException if a the predicate fails to meet a rule.
     */
    fun checkPredicateRules(rules: SqlPredicateValidationRules){
        if (isFiltered()){
            sqlPredicate!!.checkPredicateRules(
                    rules.acceptedAttributes,
                    rules.prohibitPredicates,
                    rules.prohibitAttributes,
                    rules.prohibitPredicates,
                    rules.attributeCustomRules
            )
        }
    }

    /**
     * Can also be cast as SqlPredicate.class
     */
    fun <K, V> toPredicate(validationRules: SqlPredicateValidationRules? = null): Predicate<K, V> {
        return if (isFiltered()){
            validationRules?.let {
                // If there are validation rules, then check them:
                kotlin.runCatching {
                    checkPredicateRules(validationRules)
                }.getOrElse {
                    // If there are Filter Validation Errors
                    throw FilterException(it.message
                            ?: "Invalid filter parameter", sqlPredicate.toString(), it)
                }
            }
            sqlPredicate as Predicate<K, V>

        } else {
            Predicates.alwaysTrue()
        }
    }
}