package formsmanager.forms.submission

import formsmanager.forms.domain.FormEntity
import formsmanager.forms.domain.FormSchemaEntity
import formsmanager.forms.validator.FormSubmission
import io.reactivex.Single
import org.apache.shiro.subject.Subject


interface SubmissionStrategy {

    /**
     * Process a form submission.
     * Provides all of the reference data needed to make processing decisions: The FormSubmission, Form Entity, Schema, Subject, and DryRun Indicator.
     * DryRun indicator is to dictate if the processor should process the data into a potential backend or just validate.
     * DryRun is typically used for testing or server side validation tests where you want the validator/processor to run, but you do not want the processor to push the results to another system.
     * It is up to the implementation to decide exactly how DryRun is used.
     */
    fun process(formSubmission: FormSubmission, formEntity: FormEntity?, formSchemaEntity: FormSchemaEntity?, dryRun: Boolean, subject: Subject?): Single<FormSubmissionResponse>

}