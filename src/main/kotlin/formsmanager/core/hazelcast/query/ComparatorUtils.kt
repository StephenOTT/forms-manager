package formsmanager.core.hazelcast.query

import com.hazelcast.function.ComparatorEx

class ComparatorUtils {
    companion object {
        /**
         * Creates a ComparatorEx and allows to indicate a reversed boolean.
         * This function exists to simplify the creation of comparators.
         */
        fun <ID, E> comparator(reversed: Boolean = false, compareLogic: (o1: MutableMap.MutableEntry<ID, E>, o2: MutableMap.MutableEntry<ID, E>) -> Int): ComparatorEx<MutableMap.MutableEntry<ID, E>> {
            return if (reversed) {
                ComparatorEx<MutableMap.MutableEntry<ID, E>> { o1, o2 ->
                    compareLogic.invoke(o1, o2)
                }.reversed()
            } else {
                ComparatorEx<MutableMap.MutableEntry<ID, E>> { o1, o2 ->
                    compareLogic.invoke(o1, o2)
                }
            }
        }
    }
}

