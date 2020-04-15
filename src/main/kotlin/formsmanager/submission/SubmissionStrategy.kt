package formsmanager.submission

import com.fasterxml.jackson.annotation.JsonInclude
import formsmanager.service.FormService
import formsmanager.service.FormValidationService
import formsmanager.validator.FormSubmission
import formsmanager.validator.ValidationResponseValid
import io.micronaut.context.annotation.Requires
import io.reactivex.Single
import javax.inject.Singleton


data class FormSubmissionResponse(
        val submission: ValidationResponseValid,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Map<String, Any?> = mapOf()
)


interface SubmissionStrategy {

    fun process(formSubmission: FormSubmission): Single<FormSubmissionResponse>

}


/**
 * Default Submission Handler
 * Uses the FormService for validation of a Form Submission, but you can replace with whatever service you like
 * See usage of `@Replaces` annotation
 */
@Singleton
class SubmissionHandler(
        private val formValidationService: FormValidationService
) : SubmissionStrategy{
    override fun process(formSubmission: FormSubmission): Single<FormSubmissionResponse> {
        // Where you can fully customize the Form Submission Handling
        return formValidationService.validationFormSubmissionAsTask(formSubmission).map {
            FormSubmissionResponse(it)
        }
    }
}