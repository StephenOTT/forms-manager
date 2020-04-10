package formsmanager.domain

import com.fasterxml.jackson.annotation.JsonInclude

data class FormSchema(
        val display: String,

        val components: List<Map<String, Any>>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val settings: Map<String, Any> = mapOf()
) {}