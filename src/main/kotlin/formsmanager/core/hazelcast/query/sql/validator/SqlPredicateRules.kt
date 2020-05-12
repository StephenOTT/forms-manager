package formsmanager.core.hazelcast.query.sql.validator

import com.hazelcast.query.Predicate
import com.hazelcast.query.impl.predicates.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object SqlPredicateRules {

    // @TODO convert this configuration

    /**
     * Structure rules for control of underlying rules:
     * Example: max string length of the entire sql predicate.
     */
    object StructureRules {
        val maxSqlStringLength: Int = 255
//        val maxInItems: Int = 5
    }

    /**
     * SqlPredicate special keywords
     */
    object Keywords {
        const val ENTRY_KEY_ATTRIBUTE_KEYWORD = "__key"
        const val THIS_KEYWORD = "this"
        const val ANY_KEYWORD = "[any]"
        const val MULTIPLE_CHARACTERS_KEYWORD = "%"
        const val SINGLE_CHARACTER_KEYWORD = "_"
    }

    /**
     * Regex patterns for validation of specific structures in a SqlPredicate
     * Example: Array Accessors `myfield[0]`
     * Example: Property Dot Notation `myfield.subfield.subsubfield`
     */
    object AccessorRegexes {
        const val ARRAY_ACCESSOR_REGEX = "\\[.\\]"
        const val PROPERTY_DOT_ACCESSOR_REGEX = "\\."
    }

    /**
     * Enum of Predicates used in the SqlPredicate
     */
    enum class SqlPredicates{
        /**
         * The RegexPredicate.class
         */
        REGEX{
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return RegexPredicate::class
            }
        },
        /**
         * The AndPredicate.class
         */
        AND {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return AndPredicate::class
            }
        },
        /**
         * The OrPredicate.class
         */
        OR {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return OrPredicate::class
            }
        },
        /**
         * The EqualPredicate.class
         */
        EQUAL {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return EqualPredicate::class
            }
        },
        /**
         * The NotEqualPredicate.class
         */
        NOT_EQUAL {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return NotEqualPredicate::class
            }
        },
        /**
         * The GreaterLessPredicatePredicate.class
         */
        EQUAL_LESS_GREATER {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return GreaterLessPredicate::class
            }
        },
        /**
         * The LikePredicate.class
         */
        LIKE {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return LikePredicate::class
            }
        },
        /**
         * The ILikePredicate.class
         */
        ILIKE {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return ILikePredicate::class
            }
        },
        /**
         * The BetweenPredicate.class
         */
        BETWEEN {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return BetweenPredicate::class
            }
        },
        /**
         * The InPredicate.class
         */
        IN {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return InPredicate::class
            }
        },
        /**
         * The NotPredicate.class
         */
        NOT {
            override fun predicateClass(): KClass<out Predicate<*, *>> {
                return NotPredicate::class
            }
        };

        abstract fun predicateClass(): KClass<out Predicate<*,*>>

        companion object {

            /**
             * Compound Predicates (typically `AND` & `OR`)
             */
            fun compounds(): List<SqlPredicates>{
                return listOf(AND, OR)
            }

            /**
             * List predicates / Predicates that support multiple arguments (typically `BETWEEN` & `IN`)
             */
            fun lists(): List<SqlPredicates>{
                return listOf(BETWEEN, IN)
            }

            /**
             * Predicates that support wildcards (typically `REGEX` & `ILIKE`)
             */
            fun wildcardSupported(): List<SqlPredicates>{
                return listOf(REGEX, ILIKE)
            }

        }
    }

    /**
     * Create a Attribute Rule which provides a function for custom rule injection
     */
    data class AttributeRule(val rule: (attributeName: String, predicate: Predicate<*, *>) -> Unit) {}

    /**
     * Create a Attribute Rule which provides a function for custom rule injection
     */
    fun attributeRule(rule: (attributeName: String, predicate: Predicate<*, *>) -> Unit): AttributeRule {
        return AttributeRule(rule)
    }

    /**
     * String processing of SqlPredicate
     * Typical usage is for checking against static configurations such as a max filter string length.
     */
    fun processSqlStructure(sqlPredicate: SqlPredicate) {
        val predicateString = sqlPredicate.toString()

        require(predicateString.length <= StructureRules.maxSqlStringLength) {
            "Query is longer than max ${StructureRules.maxSqlStringLength} character length."
        }

    }

    /**
     * Primary Rule processor for validating a SqlPredicate.
     * This is a recursive function that will navigate nested predicates (such as with `AND`, `OR`, & `NOT`)
     */
    fun process(predicate: Predicate<*, *>,
                acceptedAttributes: List<String>? = null,
                acceptedPredicates: List<KClass<out Predicate<*, *>>>? = null,
                prohibitAttributes: List<String>? = null,
                prohibitPredicates: List<KClass<out Predicate<*, *>>>? = null,
                attributeCustomRules: List<AttributeRule>? = null) {

        // @TODO (low priority) refactor to support any style of predicate that implements the proper interfaces.

        when (predicate) {
            is AndPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`AND` is not a supported query syntax" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`AND` is not a supported query syntax" })
                }

                predicate.getPredicates<Any, Any>().forEach {
                    process(it, acceptedAttributes, acceptedPredicates, prohibitAttributes, prohibitPredicates, attributeCustomRules)
                }

            }
            is OrPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`OR` is not a supported query syntax" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`OR` is not a supported query syntax" })
                }

                predicate.getPredicates<Any, Any>().forEach {
                    process(it, acceptedAttributes, acceptedPredicates, prohibitAttributes, prohibitPredicates, attributeCustomRules)
                }
            }
            is EqualPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Equality is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    require(!it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Equality is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    require(it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    rules.forEach { rule ->
                        rule.rule.invoke(predicate.attribute, predicate)
                    }
                }
            }
            is NotEqualPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Negated Equality is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Negated Equality is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    rules.forEach { rule ->
                        rule.rule.invoke(attribute, predicate)
                    }
                }
            }
            is GreaterLessPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Comparison is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    require(!it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Comparison is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    require(it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    rules.forEach { rule ->
                        rule.rule.invoke(predicate.attribute, predicate)
                    }
                }
            }
            is RegexPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`REGEX` is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`REGEX` is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    rules.forEach { rule ->
                        val attribute = predicate.getAttributeNameValueWithReflection()
                        rule.rule.invoke(attribute, predicate)
                    }
                }
            }
            is LikePredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`LIKE` is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`LIKE` is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    rules.forEach { rule ->
                        rule.rule.invoke(attribute, predicate)
                    }
                }
            }
            is ILikePredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`ILIKE` is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`ILIKE` is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    rules.forEach { rule ->
                        rule.rule.invoke(attribute, predicate)
                    }
                }
            }
            is BetweenPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`BETWEEN` is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    require(!it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`BETWEEN` is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    require(it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    rules.forEach { rule ->
                        rule.rule.invoke(predicate.attribute, predicate)
                    }
                }
            }
            is InPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`IN` is not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`IN` is not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(it.contains(attribute), lazyMessage = { "$attribute is not supported query attribute" })
                }
                attributeCustomRules?.let { rules ->
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    rules.forEach { rule ->
                        rule.rule.invoke(attribute, predicate)
                    }
                }
            }
            is NotPredicate -> {
                val subPredicate = predicate.predicate
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "`NOT` is not a supported query syntax" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "`NOT` is not a supported query syntax" })
                }
                process(subPredicate, acceptedAttributes, acceptedPredicates, prohibitAttributes, prohibitPredicates, attributeCustomRules)
            }
        }
    }

    /**
     * Gets the `attributeName` property in AbstractPredicate implementations.
     * Used because in some Predicates that extend from AbstractPredicate, the getAttributeName() method was not implemented.
     * Related to issue: https://github.com/hazelcast/hazelcast/issues/16945
     */
    private fun AbstractPredicate<*, *>.getAttributeNameValueWithReflection(): String {
        val attribute = this::class.memberProperties.single {
            it.name == "attributeName"
        } as KProperty1<Any, String>

        attribute.isAccessible = true

        return attribute.get(this)
    }


    /**
     * Common rules for SqlPredicate validation
     */
    object CommonRules {

        /**
         * AttributeRule that denies use of Property Dot Accessors (`myProp.mySubProp.mySubSubProp`)
         */
        val noPropertyDotAccessors = attributeRule { attributeName, predicate ->
            require(!attributeName.contains(Regex(AccessorRegexes.PROPERTY_DOT_ACCESSOR_REGEX))) {
                "Property Dot Accessors are not supported"
            }
        }

        /**
         * AttributeRule that denies the use of the `this` keyword in a property
         */
        val noThisKeyword = attributeRule { attributeName, predicate ->
            require(!attributeName.startsWith(Keywords.THIS_KEYWORD)) {
                "`this` keyword is not supported."
            }
        }

        /**
         * AttributeRule that denies the use of the `__key` keyword in a property
         */
        val noEntryKeyKeyword = attributeRule { attributeName, predicate ->
            require(!attributeName.contains(Keywords.ENTRY_KEY_ATTRIBUTE_KEYWORD)) {
                "`__key` keyword is not supported."
            }
        }

        /**
         * AttributeRule that denies the use of `any` in a Array Accessor
         */
        val noArrayAnyAccessor = attributeRule { attributeName, predicate ->
            require(!attributeName.contains(Keywords.ANY_KEYWORD)) {
                "Array `any` Accessors is not supported."
            }
        }

        /**
         * AttributeRule that denies the use of all Array Accessors
         */
        val noArrayAccessors = attributeRule { attributeName, predicate ->
            require(!attributeName.contains(Regex(AccessorRegexes.ARRAY_ACCESSOR_REGEX))) {
                "Array Accessors are not supported."
            }
        }

    }
}