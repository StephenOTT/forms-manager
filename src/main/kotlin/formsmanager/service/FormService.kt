package formsmanager.service

import formsmanager.domain.FormEntity
import formsmanager.domain.FormSchemaEntity
import formsmanager.hazelcast.task.TaskManager
import formsmanager.ifDebugEnabled
import formsmanager.respository.FormHazelcastRepository
import formsmanager.respository.FormSchemaHazelcastRepository
import formsmanager.submission.FormSubmissionResponse
import formsmanager.submission.SubmissionHandler
import formsmanager.validator.*
import io.micronaut.data.model.Pageable
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.Flowable
import io.reactivex.Single
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton

/**
 * Primary Entry point for working with Forms
 * The Form Service provides the various functions for BREAD of Forms and Form Schemas, as well as Form Schema validation.
 */
@Singleton
class FormService(
        private val formHazelcastRepository: FormHazelcastRepository,
        private val formSchemaHazelcastRepository: FormSchemaHazelcastRepository,
        private val formValidatorClient: FormValidatorClient,
        private val formValidationService: FormValidationService,
        private val submissionHandler: SubmissionHandler
) {

    companion object {
        private val log = LoggerFactory.getLogger(FormService::class.java)
    }

    /**
     * Create/insert a Form
     * @param formEntity Form to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun createForm(formEntity: FormEntity, subject: Subject? = null): Single<FormEntity> {
        return Single.fromCallable {
//            subject?.let {
//                subject.checkPermission(WildcardPermission("forms:create:${formEntity.owner}:${formEntity.tenant}"))
//            }
        }.flatMap {
            formHazelcastRepository.create(formEntity)
        }
    }

    /**
     * Get/find a Form
     * @param formId Form ID
     */
    fun getForm(formId: UUID, tenant: UUID? = null, subject: Subject? = null): Single<FormEntity> {
        return formHazelcastRepository.find(formId).map { fe ->
            tenant?.let {
                require(it == fe.tenant, lazyMessage = {"Invalid Tenant Match."})
            }
            subject?.let { sub->
                subject.checkPermission(WildcardPermission("forms:read:${fe.owner}:${fe.tenant}"))
            }
            fe
        }
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
        return formHazelcastRepository.update(formEntity) { originalItem, newItem ->
            //Update logic for automated fields @TODO consider automation with annotations
            newItem.copy(
                    ol = originalItem.ol + 1,
                    id = originalItem.id,
                    type = originalItem.type,
                    createdAt = originalItem.createdAt,
                    updatedAt = Instant.now()
            )
        }
    }

    /**
     * Update/overwrite Form Schema
     * @param formSchemaEntity Form Schema to be updated/overwritten
     */
    fun updateFormSchema(formId: UUID, schemaEntity: FormSchemaEntity, isDefault: Boolean? = false): Single<FormSchemaEntity> {
        return formHazelcastRepository.find(formId).flatMap { form ->
            require(formId == schemaEntity.formId) {
                "Form $formId does not match Id in provided schema."
            }
            //@TODO consider a lock for form

            // Create the form schema
            formSchemaHazelcastRepository.update(schemaEntity) { originalItem, newItem ->
                // Update logic for automated fields @TODO consider automation with annotations
                newItem.copy(
                        ol = originalItem.ol + 1,
                        id = originalItem.id,
                        createdAt = originalItem.createdAt,
                        updatedAt = Instant.now()
                )
            }.map { entity ->
                Pair(form, entity)
            }

        }.flatMap {
            //If the form schema was created then update the Form with the default schema ID
            formHazelcastRepository.update(it.first) { old, new ->
                old.copy(defaultSchema = new.defaultSchema)
            }.map { res ->
                Pair(res, it.second)
            }
        }.map {
            // Return the schema
            it.second
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
            require(formId == schemaEntity.formId) {
                "Form $formId does not match Id in provided schema."
            }
            //@TODO consider a lock for form

            // Create the form schema
            formSchemaHazelcastRepository.create(schemaEntity).map { createdSchema ->
                Pair(form, createdSchema)
            }

        }.flatMap {
            //If the form schema was created then update the Form with the default schema ID
            formHazelcastRepository.update(it.first) { old, new ->
                old.copy(defaultSchema = new.defaultSchema)
            }.map { res ->
                Pair(res, it.second)
            }
        }.map {
            // Return the schema
            it.second
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
                .onErrorResumeNext {
                    Single.error(IllegalArgumentException("Cannot find schema id"))
                }
    }

    /**
     * Get all schemas for the given form Id.
     * @param formId the Form Id that the Schemas belong to.
     */
    fun getAllSchemas(formId: UUID, pageable: Pageable = Pageable.from(0)): Flowable<FormSchemaEntity> {
        return formHazelcastRepository.exists(formId).map {
            if (it) {
                formId
            } else {
                throw IllegalArgumentException("Form $formId cannot be found.")
            }
        }.toFlowable().flatMap { uuid ->
            formSchemaHazelcastRepository.getSchemasForForm(uuid, pageable)
        }
    }

    fun validateFormSubmission(submission: FormSubmission): Single<Map<String, Any?>> {
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

        fun validationFormSubmissionAsTask(formSubmission: FormSubmission): Single<ValidationResponseValid> {
            return formValidationService.validationFormSubmissionAsTask(formSubmission)
        }

//    fun validationFormSubmissionAsTask(formSubmission: FormSubmission): Single<ValidationResponseValid> {
//        return taskManager.submit("form-submission-validator",
//                FormSubmissionValidatorTask(formSubmission)).map {
//            when (it) {
//                is ValidationResponseValid -> {
//                    log.ifDebugEnabled { "Got A Validation Success result: $it" }
//                    it
//
//                }
//                is ValidationResponseInvalid -> {
//                    log.ifDebugEnabled { "Got A Validation Failure result: $it" }
//                    throw FormValidationException(it)
//
//                }
//                else -> {
//                    throw IllegalStateException("Received a unknown Validation Response!!")
//                }
//            }
//        }
//    }

    /**
     * Service for handling end-to-end form submission: Submission, Validation, Routing to Submission Handler, and response from submission handler
     */
    fun processFormSubmission(formSubmission: FormSubmission): Single<FormSubmissionResponse> {
        return submissionHandler.process(formSubmission)
    }
}