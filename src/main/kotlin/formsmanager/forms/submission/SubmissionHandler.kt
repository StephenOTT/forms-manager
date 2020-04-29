package formsmanager.forms.submission

import formsmanager.forms.domain.FormEntity
import formsmanager.forms.domain.FormSchemaEntity
import formsmanager.forms.service.FormValidationService
import formsmanager.forms.validator.FormSubmission
import io.reactivex.Single
import org.apache.shiro.subject.Subject
import javax.inject.Singleton

/**
 * Default Submission Handler
 * Uses the FormService for validation of a Form Submission, but you can replace with whatever service you like
 * See usage of `@Replaces` annotation
 */
@Singleton
class SubmissionHandler(
        private val formValidationService: FormValidationService
) : SubmissionStrategy {

    /**
     * Generic Processor
     */
    override fun process(formSubmission: FormSubmission, formEntity: FormEntity?, formSchemaEntity: FormSchemaEntity?, dryRun: Boolean, subject: Subject?): Single<FormSubmissionResponse> {
        // Where you can fully customize the Form Submission Handling
        return formValidationService.validationFormSubmissionAsTask(formSubmission).map {
            FormSubmissionResponse(it)
        }
    }
}