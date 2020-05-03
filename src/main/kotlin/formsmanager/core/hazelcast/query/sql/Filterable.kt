package formsmanager.core.hazelcast.query.sql

import com.fasterxml.jackson.annotation.JsonCreator
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import com.hazelcast.query.impl.predicates.SqlPredicate

interface Filterable {

    val sqlPredicate: SqlPredicate?
    val unfiltered: Boolean

    companion object {
        const val FILTER_PARAMETER = "filter" //@TODO move to config

        @JsonCreator
        fun from(rawString: String): Filterable {
            return DefaultFilterable(SqlPredicate(rawString))
        }

        fun unfiltered(): Filterable {
            return DefaultFilterable()
        }
    }

    fun isFiltered(): Boolean{
        return !unfiltered
    }

    /**
     * Validates Predicate Rules for SqlPredicate processing
     * If is not filtered isFiltered == false, then rules are not processed
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
                    throw FilterValidationException(it.message
                            ?: "Invalid filter parameter", it)
                }
            }
            sqlPredicate as Predicate<K, V>

        } else {
            Predicates.alwaysTrue()
        }
    }
}