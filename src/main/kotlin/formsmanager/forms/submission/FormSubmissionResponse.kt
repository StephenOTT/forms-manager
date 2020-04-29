package formsmanager.forms.submission

import com.fasterxml.jackson.annotation.JsonInclude
import formsmanager.forms.validator.ValidationResponseValid

data class FormSubmissionResponse(
        val submission: ValidationResponseValid,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Map<String, Any?> = mapOf()
)