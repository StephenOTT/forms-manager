package formsmanager.forms.validator

import com.fasterxml.jackson.annotation.JsonInclude
import formsmanager.forms.domain.FormioFormSchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class FormSubmission(
        val schema: FormioFormSchema,
        val submission: FormSubmissionData)

@Schema
data class FormSubmissionData(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Map<String, Any?>,
        val metadata: Map<String, Any?>?)



