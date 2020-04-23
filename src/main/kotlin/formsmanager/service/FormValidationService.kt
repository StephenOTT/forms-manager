package formsmanager.service

import formsmanager.hazelcast.task.TaskManager
import formsmanager.ifDebugEnabled
import formsmanager.validator.*
import io.reactivex.Single
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * Service providing Form Validation functions
 */
@Singleton
class FormValidationService(
        private val taskManager: TaskManager
) {

    private val log = LoggerFactory.getLogger(FormValidationService::class.java)

    fun validationFormSubmissionAsTask(formSubmission: FormSubmission): Single<ValidationResponseValid> {
        return taskManager.submit("form-submission-validator",
                FormSubmissionValidatorTask(formSubmission)).map {
            when (it) {
                is ValidationResponseValid -> {
                    log.ifDebugEnabled { "Got A Validation Success result: $it" }
                    it

                }
                is ValidationResponseInvalid -> {
                    log.ifDebugEnabled { "Got A Validation Failure result: $it" }
                    throw FormValidationException(it)

                }
                else -> {
                    throw IllegalStateException("Received a unknown Validation Response!!")
                }
            }
        }
    }

}