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
import io.micronaut.http.HttpResponse
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
    fun createForm(formEntity: FormEntity, tenant: UUID, subject: Subject? = null): Single<FormEntity> {
        return Single.fromCallable {
            require(tenant == formEntity.tenant, lazyMessage = { "Invalid Tenant Match." })

            subject?.let {
                subject.checkPermission(WildcardPermission("forms:create:${formEntity.owner}:${formEntity.tenant}"))
            }
        }.flatMap {
            formHazelcastRepository.create(formEntity)
        }
    }

    /**
     * Get/find a Form
     * @param formId Form ID
     */
    fun getForm(formId: UUID, tenant: UUID, subject: Subject? = null): Single<FormEntity> {
        return formHazelcastRepository.find(formId).map { fe ->
            require(tenant == fe.tenant, lazyMessage = { "Invalid Tenant Match." })

            subject?.let {
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
    fun updateForm(formEntity: FormEntity, tenant: UUID, subject: Subject? = null): Single<FormEntity> {
        return getForm(formEntity.id, tenant).map { fe ->
            require(tenant == fe.tenant, lazyMessage = { "Invalid Tenant Match." })

            subject?.let {
                subject.checkPermission(WildcardPermission("forms:update:${fe.owner}:${fe.tenant}"))
            }
        }.flatMap {
            formHazelcastRepository.update(formEntity) { originalItem, newItem ->
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
    }

    /**
     * Update/overwrite Form Schema
     * @param formSchemaEntity Form Schema to be updated/overwritten
     */
    fun updateFormSchema(formId: UUID, schemaEntity: FormSchemaEntity, isDefault: Boolean = false, tenant: UUID, subject: Subject? = null): Single<FormSchemaEntity> {
        return getForm(formId, tenant).map { fe ->
            require(fe.id == schemaEntity.formId) {
                "Form $formId does not match Id in provided schema."
            }

            if (isDefault) {
                // Check if they are allowed to update the Form with the Default key.
                // They have the permission to update the Schema, but not the permission to make it the default form.
                subject?.let {
                    subject.checkPermission(WildcardPermission("forms:update:${fe.owner}:${fe.tenant}"))
                }
            }
            fe
        }.flatMap { fe ->

            subject?.let {
                subject.checkPermission(WildcardPermission("form_schemas:update:${fe.owner}:${fe.tenant}"))
            }

            formSchemaHazelcastRepository.update(schemaEntity) { originalItem, newItem ->
                // Update logic for automated fields @TODO consider automation with annotations
                newItem.copy(
                        ol = originalItem.ol + 1,
                        id = originalItem.id,
                        createdAt = originalItem.createdAt,
                        updatedAt = Instant.now()
                )
            }.map { entity ->
                Pair(fe, entity)
            }

        }.flatMap {
            //@TODO consider adding a lock on the form entity incase of race condition between update of schema and update of form with default value.
            //If the form schema was created then update the Form with the default schema ID
            formHazelcastRepository.update(it.first) { old, new ->
                old.copy(
                        ol = old.ol + 1,
                        defaultSchema = new.defaultSchema
                )

            }.map { res ->
                Pair(res, it.second)
            }

        }.map {
            it.second
        }
    }

    /**
     * Create/insert a form schema for a specific form id
     * @param formId Form ID
     * @param schemaEntity Form Schema
     * @param isDefault if the schema should be made the default schema for the Form.  This will make a update to the Form object.
     */
    fun createSchema(formId: UUID, schemaEntity: FormSchemaEntity, isDefault: Boolean = false, tenant: UUID, subject: Subject? = null): Single<FormSchemaEntity> {
        return getForm(formId, tenant).map { fe ->
            require(fe.id == schemaEntity.formId) {
                "Form $formId does not match Id in provided schema."
            }

            if (isDefault) {
                // Check if they are allowed to update the Form with the Default key.
                // They have the permission to update the Schema, but not the permission to make it the default form.
                subject?.let {
                    subject.checkPermission(WildcardPermission("forms:update:${fe.owner}:${fe.tenant}"))
                }
            }
            fe
        }.flatMap { fe ->

            subject?.let {
                subject.checkPermission(WildcardPermission("form_schemas:create:${fe.owner}:${fe.tenant}"))
            }

            // Create the form schema
            formSchemaHazelcastRepository.create(schemaEntity).map { createdSchema ->
                Pair(fe, createdSchema)
            }

        }.flatMap {
            //@TODO consider adding a lock on the form entity incase of race condition between update of schema and update of form with default value.
            //If the form schema was created then update the Form with the default schema ID
            formHazelcastRepository.update(it.first) { old, new ->
                old.copy(
                        ol = old.ol + 1,
                        defaultSchema = new.defaultSchema
                )

            }.map { res ->
                Pair(res, it.second)
            }

        }.map {
            it.second
        }
    }

    /**
     * Get the default schema for the given form Id.
     * @param formId The Form Id
     */
    fun getDefaultSchema(formId: UUID, tenant: UUID, subject: Subject? = null): Single<FormSchemaEntity> {
        return getForm(formId, tenant, subject).map { fe ->
            if (fe.defaultSchema != null) {
                Pair(fe.defaultSchema, fe)
            } else {
                throw IllegalArgumentException("Default Schema is not set for form $formId")
            }
        }.flatMap {
            //@TODO this could be optimized to prevent getSchema from recalling getForm
            getSchema(it.first, tenant, subject)
        }
    }

    fun getSchema(schemaId: UUID, tenant: UUID, subject: Subject? = null): Single<FormSchemaEntity> {
        return formSchemaHazelcastRepository.find(schemaId)
                .onErrorResumeNext {
                    Single.error(IllegalArgumentException("Cannot find schema id"))
                }.flatMap { fse ->
                    getForm(fse.formId, tenant, subject).map { fe ->
                        Pair(fe, fse)
                    }
                }.map { (fe, fse) ->
                    subject?.let {
                        subject.checkPermission(WildcardPermission("form_schemas:read:${fe.owner}:${fe.tenant}"))
                    }
                    fse
                }
    }

    /**
     * Get all schemas for the given form Id.
     * @param formId the Form Id that the Schemas belong to.
     */
    fun getAllSchemas(formId: UUID, tenant: UUID, subject: Subject? = null, pageable: Pageable = Pageable.from(0)): Flowable<FormSchemaEntity> {
        return getForm(formId, tenant).map { fe ->
            subject?.let {
                subject.checkPermission(WildcardPermission("form_schemas:read:${fe.owner}:${fe.tenant}"))
            }
        }.flatMapPublisher {
            formSchemaHazelcastRepository.getSchemasForForm(formId, pageable)
        }
    }

    /**
     * Used by Tasks and other internal systems for form submission validation.
     * DOES NOT PROVIDE ANY PERMISSION CHECKS
     * ONLY SHOULD BE USED FOR INTERNAL CALLS
     * USE processFormSubmission() for Permission Checks and normal flow.
     */
    fun validateFormSubmission(submission: FormSubmission): Single<Map<String, Any?>> {
        return formValidatorClient.validate(submission)
                .onErrorResumeNext {
                    // @TODO Can eventually be replaced once micronaut-core fixes a issue where the response body is not passed to @Error handler when it catches the HttpClientResponseException
                    // @TODO Review for cleanup
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

//    fun validationFormSubmissionAsTask(formOwner: UUID, formTenant: UUID, schemaId: UUID, subject: Subject? = null, formSubmission: FormSubmission): Single<ValidationResponseValid> {
//        return formValidationService.validationFormSubmissionAsTask(formSubmission)
//    }


    /**
     * Service for handling end-to-end form submission: Submission, Validation, Routing to Submission Handler, and response from submission handler
     */
    fun processFormSubmission(schemaId: UUID, tenant: UUID, formSubmission: FormSubmission, dryRun: Boolean = false, subject: Subject? = null): Single<FormSubmissionResponse> {
        // @TODO refactor to reduce amount of recalls to getSchema, getForm, etc, as Getting Schema also gets the Form.
        return getSchema(schemaId, tenant, subject).flatMap { fse ->
            getForm(fse.formId, tenant, subject).map { fe ->
                Pair(fe, fse)
            }
        }.flatMap { (fe, fse) ->
            subject?.let {
                subject.checkPermission(WildcardPermission("form_schemas:validate:${fe.owner}:${fe.tenant}"))
            }
            submissionHandler.process(formSubmission, fe, fse, dryRun, subject)
        }
    }
}