package formsmanager.core.hazelcast.query.sql.binder

import formsmanager.core.hazelcast.query.sql.SqlPredicateRules


/**
 * Provided Filterable restrictions for use with Filterable binder.
 * Apply this annotation on arguments in Controller methods.
 * `fun search(subject: Subject, @RestrictFilter(allowedProperties = "name") filter: Filterable)`
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class FilterableControl(
        /**
         * Override the filter Name.  default is `filter.
         * Currently not used, but will be used in the future to allow multiple filter parameters
         */
        val filterName: String = "filter",

        /**
         * What properties are not allowed to be used.
         * Using this control means: "Allow all properties except for the following".
         * If this property is used, the allowedProperties control is ignored.
         */
        val prohibitProperties: Array<String> = [],

        /**
         * What Predicate types are not allowed to be used.
         * Using this control means: "Allow all Predicates except for the following".
         * If this property is used, the allowedTypes control is ignored.
         */
        val prohibitTypes: Array<SqlPredicateRules.SqlPredicates> = [],

        /**
         * What properties are allowed to be used.
         * Using this control means: "Prohibit all properties except for the following".
         */
        val allowProperties: Array<String> = [],

        /**
         * What Predicate types are allowed to be used.
         * Using this control means: "Prohibit all Predicate Types except for the following".
         */
        val allowTypes: Array<SqlPredicateRules.SqlPredicates> = [],

        /**
         * If the `this` keyword can be used in the filter
         * Defaults to false.
         */
        val allowThisKeyword: Boolean = false,

        /**
         * If the `__key` keyword can be used in the filter.
         * Defaults to false.
         */
        val allowEntryKeyAttributeKeyword: Boolean = false,

        /**
         * If the `any` keyword can be used in Array accessors.
         * Defaults to true.
         */
        val allowArrayAnyKeyword: Boolean = true,

        /**
         * If the array accessor can be used to select specific items in a array.
         * Defaults to false.
         */
        val allowArrayItemNumberAccessor: Boolean = false
)