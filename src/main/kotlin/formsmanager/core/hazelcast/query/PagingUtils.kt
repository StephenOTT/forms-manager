package formsmanager.core.hazelcast.query

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import com.hazelcast.function.ComparatorEx
import com.hazelcast.query.PagingPredicate
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.hazelcast.query.ComparatorUtils.Companion.comparator
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort

class PagingUtils {

    companion object {

        fun <ID: Any, E: Any> createPagingPredicateComparators(entityBeanDescription: BeanDescription, pageable: Pageable): List<ComparatorEx<MutableMap.MutableEntry<ID, E>>>{
            return pageable.sort.orderBy.map { sortOrder ->
                //For Each property defined in the Sort object of Pageable,
                // Gets the jackson BeanProperty using a match of the Jackson json property and the sort order property.
                // This is allows us to get the pojo's actual property/getter and the return type
                val prop: BeanPropertyDefinition = entityBeanDescription.findProperties().single { jProp -> jProp.name == sortOrder.property }
                val returnType: Class<*> = prop.getter.rawReturnType

                // This whole if section should be moved into a Bean Singleton Detection Strategy:

                // Ensure the return type implements Comparable
                if (returnType::class is Comparable<*>) {
                    // convert to generics and move to another method
                    // Make note for future Hazelcast devs that having to define the Map Key type is "annoying", because when doing a query on the value, we dont want to have to know what the key type was...
                    // @TODO Add a custom registry so that you can add Beans that implement specfic comparator rules based on the types with a fallback to using the Comparable
                    // Create a Comparator (Hazelcast's ComparatorEx) to compare the two values
                    val comp = comparator<ID, E> { o1, o2 ->
                        //@TODO needs review:
                        // Get the underlying property value from the two objects and compare them using the Comparable's compareTo.
                        // The casting to Comparable<Any> was required or else the compareTo does not work in the compiler.
                        val o1Prop = o1.value::class.members.single { it.name == prop.internalName }.call(o1.value) as Comparable<Any>
                        val o2Prop = o2.value::class.members.single { it.name == prop.internalName }.call(o2.value) as Comparable<Any>
                        o1Prop.compareTo(o2Prop)
                    }

                    // Check what the sort order is to set the reversed if defined.
                    when (sortOrder.direction) {
                        Sort.Order.Direction.ASC -> {
                            comp
                        }
                        Sort.Order.Direction.DESC -> {
                            comp.reversed()
                        }
                        else -> {
                            // Backup for future changes to Micronaut's Sort order in Pageable
                            throw IllegalStateException("Unexpected Sort Order Direction was provided.")
                        }
                    }
                } else {
                    // The entity must implement Comparable
                    throw IllegalArgumentException("Unable to setup comparable for sort property json:${prop.name} / internal: ${prop.internalName}")
                }
            }
        }

        fun <ID: Any, E: Any> createPagingPredicate(filter: Predicate<ID, E>, comparators: List<ComparatorEx<MutableMap.MutableEntry<ID, E>>>, pageSize: Int, page: Int = 0): PagingPredicate<ID, E>{
            val predicate = Predicates.pagingPredicate(filter, comparators.first(), pageSize)
            // Set the specific page number to return.  This is done because the pagingPredicate constructor does not provide this.
            predicate.page = page
            return predicate
        }
    }

}