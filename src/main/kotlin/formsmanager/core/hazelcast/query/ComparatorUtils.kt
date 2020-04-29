package formsmanager.core.hazelcast.query

import com.hazelcast.function.ComparatorEx

class ComparatorUtils {
    companion object {
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

