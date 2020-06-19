package formsmanager.camunda.hazelcast.variable

import com.hazelcast.map.IMap
import formsmanager.camunda.hazelcast.session.HazelcastTransactionSession
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.ObjectValue
import org.camunda.bpm.engine.variable.value.TypedValue
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class HazelcastVariableSerializer(
        @param:Named("camunda-process-instance-process-variables")
        private val camundaVariablesMap: IMap<String, ProcessVariable>
) : AbstractTypedValueSerializer<ObjectValue>(ValueType.OBJECT) {

    companion object {
        const val NAME = "hazelcast"
        //@TODO add near cache for these variables
        const val VARIABLES_MAP_NAME = "camunda-process-instance-process-variables"
    }

    override fun getName(): String {
        return NAME
    }

    override fun isMutableValue(typedValue: ObjectValue?): Boolean {
        return super.isMutableValue(typedValue)
    }

    override fun canWriteValue(value: TypedValue): Boolean {
        return if (isDeserializedObjectValue(value) || value is UntypedValueImpl) {
            value.value is HazelcastVariable
        } else {
            false
        }
    }

    private fun isDeserializedObjectValue(value: TypedValue): Boolean {
        return value is ObjectValue && value.isDeserialized
    }

    override fun convertToTypedValue(untypedValue: UntypedValueImpl): ObjectValue {
        return Variables.objectValue(untypedValue.value, untypedValue.isTransient).create()
    }

    override fun writeValue(objectValue: ObjectValue, valueFields: ValueFields) {
        val hazelcastTransaction = Context
                .getCommandContext()
                .getSession(HazelcastTransactionSession::class.java)

        if (hazelcastTransaction == null) {
            throw ProcessEngineException("Cannot set Hazelcast variable: " + HazelcastTransactionSession::class.java + " not configured")

        } else {
            val varsMap = hazelcastTransaction.transactionContext.getMap<String, ProcessVariable>(VARIABLES_MAP_NAME)

            val value = objectValue.value as HazelcastVariable

            val processVar = ProcessVariable(
                    mapKey = value.mapKey,
                    engine = Context.getProcessEngineConfiguration().processEngine.name,
                    processInstanceId = null,
                    processDefinitionId = null,
                    processDefinitionKey = null,
                    tenantId = null,
                    createdAt = null,
                    scope = null,
                    activityInstanceId = null,
                    variableName = value.name,
                    variableValue = value.value
            )

            varsMap[value.mapKey] = processVar

            valueFields.textValue = processVar.mapKey
            valueFields.textValue2 = "hazelcast-variable"
        }
    }

    override fun readValue(valueFields: ValueFields, deserializeObjectValue: Boolean): ObjectValue {
        return if (valueFields.textValue != null && valueFields.textValue2 != null) {
            val mapKey = valueFields.textValue
            val variable = camundaVariablesMap[mapKey]

            Variables.objectValue(variable).create()

        } else {
            Variables.objectValue(null).create()
        }
    }
}