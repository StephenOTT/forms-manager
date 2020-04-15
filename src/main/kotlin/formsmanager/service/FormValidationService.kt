package formsmanager.service

import formsmanager.hazelcast.task.TaskManager
import formsmanager.ifDebugEnabled
import formsmanager.validator.*
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
class FormValidationService(
        private val taskManager: TaskManager
) {

    fun validationFormSubmissionAsTask(formSubmission: FormSubmission): Single<ValidationResponseValid> {
        return taskManager.submit("form-submission-validator",
                FormSubmissionValidatorTask(formSubmission)).map {
            when (it) {
                is ValidationResponseValid -> {
                    FormService.log.ifDebugEnabled { "Got A Validation Success result: $it" }
                    it

                }
                is ValidationResponseInvalid -> {
                    FormService.log.ifDebugEnabled { "Got A Validation Failure result: $it" }
                    throw FormValidationException(it)

                }
                else -> {
                    throw IllegalStateException("Received a unknown Validation Response!!")
                }
            }
        }
    }

}