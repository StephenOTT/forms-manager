package formsmanager.core.hazelcast.query.sql

import com.hazelcast.query.Predicate
import com.hazelcast.query.impl.predicates.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object SqlPredicateRules {

    // @TODO convert this configuration

    object StructureRules {
        val maxSqlStringLengh: Int = 255
//        val maxInItems: Int = 5
    }

    object Keywords {
        const val ENTRY_KEY_ATTRIBUTE_KEYWORD = "__key"
        const val THIS_KEYWORD = "this"
        const val ANY_KEYWORD = "any"
        const val MULTIPLE_CHARACTERS_KEYWORD = "%"
        const val SINGLE_CHARACTER_KEYWORD = "_"
    }

    object AccessorRegexes {
        const val ARRAY_ACCESSOR_REGEX = "\\[.\\]"
        const val PROPERTY_DOT_ACCESSOR_REGEX = "\\."
    }

    object Predicates {
        val REGEX = RegexPredicate::class
        val AND = AndPredicate::class
        val OR = OrPredicate::class
        val EQUAL = EqualPredicate::class
        val NOT_EQUAL = NotEqualPredicate::class
        val EQUAL_LESS_GREATER = GreaterLessPredicate::class
        val LIKE = LikePredicate::class
        val ILIKE = ILikePredicate::class
        val BETWEEN = BetweenPredicate::class
        val IN = InPredicate::class
        val NOT = NotPredicate::class

        val ALL_PREDICATES = listOf(
                REGEX, AND, OR,
                EQUAL, NOT_EQUAL, EQUAL_LESS_GREATER,
                LIKE, ILIKE, BETWEEN,
                IN, NOT
        )

        val COMPOUNDS = listOf(AND, OR)

        val LISTS = listOf(BETWEEN, IN)

        val WILDCARD_SUPPORTED = listOf(REGEX, ILIKE)

    }

    fun attributeRule(rule: (attributeName: String, predicate: Predicate<*, *>) -> Unit): AttributeRule {
        return AttributeRule(rule)
    }

    data class AttributeRule(val rule: (attributeName: String, predicate: Predicate<*, *>) -> Unit) {

    }

    object CommonRules {
        val noArrayOrPropertyDotAccessors = attributeRule { attributeName, predicate ->
            require(!attributeName.contains(AccessorRegexes.PROPERTY_DOT_ACCESSOR_REGEX), lazyMessage = { "Property Dot Accessors are not supported" })
            require(!attributeName.contains(AccessorRegexes.ARRAY_ACCESSOR_REGEX), lazyMessage = { "Array Accessors are not supported." })
        }
    }

    fun processSqlStructure(sqlPredicate: SqlPredicate) {
        val predicateString = sqlPredicate.toString()

        require(predicateString.length <= StructureRules.maxSqlStringLengh) {
            "Query is longer than max ${StructureRules.maxSqlStringLengh} character length."
        }

    }

    fun process(predicate: Predicate<*, *>,
                acceptedAttributes: List<String>? = null,
                acceptedPredicates: List<KClass<out Predicate<*, *>>>? = null,
                prohibitAttributes: List<String>? = null,
                prohibitPredicates: List<KClass<out Predicate<*, *>>>? = null,
                attributeCustomRules: List<AttributeRule>? = null) {

        when (predicate) {
            is AndPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }

                predicate.getPredicates<Any, Any>().forEach {
                    process(it, acceptedAttributes, acceptedPredicates, prohibitAttributes, prohibitPredicates, attributeCustomRules)
                }

            }
            is OrPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }

                predicate.getPredicates<Any, Any>().forEach {
                    process(it, acceptedAttributes, acceptedPredicates, prohibitAttributes, prohibitPredicates, attributeCustomRules)
                }
            }
            is EqualPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    require(!it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
            is GreaterLessPredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    require(!it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                acceptedAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
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
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
            is ILikePredicate -> {
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    require(!it.contains(predicate.attribute), lazyMessage = { "${predicate.attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                prohibitAttributes?.let {
                    val attribute = predicate.getAttributeNameValueWithReflection()
                    require(!it.contains(attribute), lazyMessage = { "${attribute} is not supported query attribute" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
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
            is NotPredicate -> {
                val subPredicate = predicate.predicate
                prohibitPredicates?.let {
                    require(!it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                acceptedPredicates?.let {
                    require(it.contains(predicate::class), lazyMessage = { "Not a supported query syntax" })
                }
                process(subPredicate, acceptedAttributes, acceptedPredicates, prohibitAttributes, prohibitPredicates, attributeCustomRules)
            }
        }
    }

    fun AbstractPredicate<*, *>.getAttributeNameValueWithReflection(): String {
        val attribute = this::class.memberProperties.single {
            it.name == "attributeName"
        } as KProperty1<Any, String>

        attribute.isAccessible = true

        return attribute.get(this)
    }
}