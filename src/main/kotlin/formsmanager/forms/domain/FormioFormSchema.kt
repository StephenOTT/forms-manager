package formsmanager.forms.domain

import com.fasterxml.jackson.annotation.JsonInclude

data class FormioFormSchema(
        val display: String,

        val components: List<Map<String, Any>>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val settings: Map<String, Any> = mapOf()
) {}