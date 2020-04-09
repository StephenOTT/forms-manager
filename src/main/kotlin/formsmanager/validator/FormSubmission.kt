package formsmanager.validator

import com.fasterxml.jackson.annotation.JsonInclude
import formsmanager.domain.FormSchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class FormSubmission(
        val schema: FormSchema,
        val submission: FormSubmissionData)

@Schema
data class FormSubmissionData(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Map<String, Any?>,
        val metadata: Map<String, Any?>?)



