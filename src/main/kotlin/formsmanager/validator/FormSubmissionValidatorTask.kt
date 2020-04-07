package formsmanager.validator

import formsmanager.hazelcast.InjectAware
import formsmanager.hazelcast.task.Task
import formsmanager.service.FormService
import formsmanager.validator.*
import io.micronaut.context.annotation.Parameter
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@InjectAware
class FormSubmissionValidatorTask(
        @Parameter val formSubmission: FormSubmission // Must be public to ensure that jackson can read th
): Task<ValidationResponse>(){

    @Inject
    private lateinit var formService: FormService

    override fun call(): ValidationResponse {
        return formService.validateFormSubmission(formSubmission)
                .onErrorResumeNext { t ->
                    if (t is FormValidationException) {
                        t.responseBody
                    } else {
                        t.printStackTrace()
                    }
                    Single.error(t)
                }.map {
                    ValidationResponseValid(it)
                }.subscribeOn(Schedulers.io()).blockingGet()
    }
}