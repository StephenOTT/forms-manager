package formsmanager.camunda.hazelcast

import com.fasterxml.jackson.annotation.JsonIgnore
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity

/**
 * Prevents conflicting setters for ByteArray Field in the HistoricVariableInstanceEntity
 */
interface HistoricVariableInstanceEntitySmileMixIn{
    @JsonIgnore fun setByteArrayValue(byteArrayValue: ByteArrayEntity?)
}