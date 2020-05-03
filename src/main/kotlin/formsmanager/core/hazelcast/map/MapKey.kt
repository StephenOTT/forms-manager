package formsmanager.core.hazelcast.map

import java.util.*

interface MapKey{
    fun toUUID(): UUID
}