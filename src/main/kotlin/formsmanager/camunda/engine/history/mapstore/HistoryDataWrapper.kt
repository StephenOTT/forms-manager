package formsmanager.camunda.engine.history.mapstore

import com.fasterxml.jackson.annotation.JsonTypeInfo

class HistoryDataWrapper(
        val clazz: String,

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "clazz")
        val data: Any?
)