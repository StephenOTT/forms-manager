package formsmanager.core.hazelcast.map

import java.util.*

/**
 * Data wrapper for LocalEntry Listener UUIDs.
 * Used for named Beans that can be linked.
 * @TODO consider moving this logic into a shared array rather than list of beans.
 */
data class LocalEntryListener(val value: UUID)