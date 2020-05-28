package formsmanager.camunda.engine.variable

import com.hazelcast.internal.util.UuidUtil

data class HazelcastVariable(
        val name: String,
        val value: Any?,
        val mapKey: String = UuidUtil.newSecureUuidString()
){
    //@TODO consider passing in a Map Name so you can send variables to different maps.
}