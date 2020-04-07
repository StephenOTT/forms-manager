package formsmanager.service

import formsmanager.domain.FormEntity
import formsmanager.domain.FormSchemaEntity
import formsmanager.hazelcast.task.TaskManager
import formsmanager.ifDebugEnabled
import formsmanager.respository.FormHazelcastRepository
import formsmanager.respository.FormSchemaHazelcastRepository
import formsmanager.validator.*
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class FormService(
        private val formHazelcastRepository: FormHazelcastRepository,
        private val formSchemaHazelcastRepository: FormSchemaHazelcastRepository,
        private val formValidatorClient: FormValidatorClient,
        private val taskManager: TaskManager
) {

    companion object {
        val log = LoggerFactory.getLogger(FormService::class.java)
    }

    /**
     * Create/insert a Form
     * @param formEntity Form to be created/inserted
     */
    fun createForm(formEntity: FormEntity): Single<FormEntity> {
        return formHazelcastRepository.create(formEntity)
    }

    /**
     * Get/find a Form
     * @param formId Form ID
     */
    fun getForm(formId: UUID): Single<FormEntity> {
        return formHazelcastRepository.find(formId)
    }

    fun formExists(formId: UUID): Single<Boolean> {
        return formHazelcastRepository.exists(formId)
    }

    fun formSchemaExists(formSchemaId: UUID): Single<Boolean> {
        return formSchemaHazelcastRepository.exists(formSchemaId)
    }

    /**
     * Update/overwrite Form
     * @param formEntity Form to be updated/overwritten
     */
    fun updateForm(formEntity: FormEntity): Single<FormEntity> {
        return formHazelcastRepository.update(formEntity) { originalItem ->
            formEntity.apply { ol = +1 }
        }
    }

    /**
     * Create/insert a form schema for a specific form id
     * @param formId Form ID
     * @param schemaEntity Form Schema
     * @param isDefault if the schema should be made the default schema for the Form.  This will make a update to the Form object.
     */
    fun createSchema(formId: UUID, schemaEntity: FormSchemaEntity, isDefault: Boolean? = false): Single<FormSchemaEntity> {
        return formHazelcastRepository.find(formId).flatMap { form ->
            require(formId == schemaEntity.formId, lazyMessage = { "Form $formId does not match Id in provided schema." })
            formSchemaHazelcastRepository.create(schemaEntity).map { createdSchema ->
                Pair(form, createdSchema)
            }
        }.map {
            if (isDefault == true) {
                formHazelcastRepository.update(it.first.apply { defaultSchema = it.second.id }, { originalItem -> it.first}).blockingGet() //@TODO FIX <----
                it.second
            } else {
                it.second
            }
        }
    }

    /**
     * Get the default schema for the given form Id.
     * @param formId The Form Id
     */
    fun getDefaultSchema(formId: UUID): Single<FormSchemaEntity> {
        return formHazelcastRepository.find(formId).map {
            if (it.defaultSchema != null) {
                it.defaultSchema
            } else {
                throw IllegalArgumentException("Default Schema is not set for form $formId")
            }
        }.flatMap { defaultSchemaUuid ->
            formSchemaHazelcastRepository.find(defaultSchemaUuid)
        }
    }

    fun getSchema(schemaId: UUID): Single<FormSchemaEntity> {
        return formSchemaHazelcastRepository.find(schemaId)
    }

    /**
     * Get all schemas for the given form Id.
     * @param formId the Form Id that the Schemas belong to.
     */
    fun getAllSchemas(formId: UUID): Single<List<FormSchemaEntity>> {
        return formHazelcastRepository.exists(formId).map {
            if (it) {
                formId
            } else {
                throw IllegalArgumentException("Form $formId cannot be found.")
            }
        }.flatMap { uuid ->
            formSchemaHazelcastRepository.getSchemasForForm(uuid)
        }
    }

    fun validateFormSubmission(submission: FormSubmission): Single<Map<String, Any?>>{
        return formValidatorClient.validate(submission)
                .onErrorResumeNext {
                    // @TODO Can eventually be replaced once micronaut-core fixes a issue where the response body is not passed to @Error handler when it catches the HttpClientResponseException
                    //@TODO Review for cleanup
                    if (it is HttpClientResponseException) {
                        val body = it.response.getBody(ValidationResponseInvalid::class.java)
                        if (body.isPresent) {
                            Single.error(FormValidationException(body.get()))
                        } else {
                            Single.error(IllegalStateException("Invalid Response Received", it))
                        }
                    } else {
                        Single.error(IllegalStateException("Unexpected Error received from Form Validation request.", it))
                    }
                }.map {
                    it.body()!!.processed_submission
                }
    }

    fun validationFormSubmissionAsTask(formSubmission: FormSubmission): Single<ValidationResponseValid>{
        return taskManager.submit("form-submission-validator",
        FormSubmissionValidatorTask(formSubmission)).map {
            when (it) {
                is ValidationResponseValid -> {
                    log.ifDebugEnabled { "Got A Validation Success result: $it"  }
                    it

                }
                is ValidationResponseInvalid -> {
                    log.ifDebugEnabled { "Got A Validation Failure result: $it"  }
                    throw FormValidationException(it)

                }
                else -> {
                    throw IllegalStateException("Received a unknown Validation Response!!")
                }
            }
        }
    }
}