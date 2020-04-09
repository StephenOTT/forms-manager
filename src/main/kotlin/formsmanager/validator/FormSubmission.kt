package formsmanager.validator

import com.fasterxml.jackson.annotation.JsonInclude
import formsmanager.domain.FormSchema

data class FormSubmission(
        val schema: FormSchema,
        val submission: FormSubmissionData)

data class FormSubmissionData(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Map<String, Any?>,
        val metadata: Map<String, Any?>?)



