package formsmanager.forms.service

import com.hazelcast.projection.Projections
import com.hazelcast.query.Predicates
import formsmanager.core.security.shiro.checkAuthorization
import formsmanager.forms.domain.Form
import formsmanager.forms.domain.FormId
import formsmanager.forms.domain.FormSchema
import formsmanager.forms.domain.FormSchemaEntityId
import formsmanager.forms.respository.FormHazelcastRepository
import formsmanager.forms.respository.FormSchemaHazelcastRepository
import formsmanager.forms.submission.FormSubmissionResponse
import formsmanager.forms.submission.SubmissionHandler
import formsmanager.forms.validator.FormSubmission
import formsmanager.forms.validator.FormValidationException
import formsmanager.forms.validator.FormValidatorClient
import formsmanager.forms.validator.ValidationResponseInvalid
import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.service.TenantService
import io.micronaut.data.model.Pageable
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
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
        private val submissionHandler: SubmissionHandler,
        private val tenantService: TenantService
) {

    companion object {
        private val log = LoggerFactory.getLogger(FormService::class.java)
    }

    /**
     * Create/insert a Form
     * @param form Form to be created/inserted
     * @param subject optional Shiro Subject.  If Subject is provided, then security validation will occur.
     */
    fun createForm(form: Form, subject: Subject? = null): Single<Form> {
        return tenantService.exists(form.tenant, true).flatMap {
            subject.checkAuthorization("forms:create:${form.owner}:${form.tenant}")
        }.flatMap {
            formHazelcastRepository.create(form)
        }
    }


    fun getForm(formMapKey: FormId, subject: Subject? = null): Single<Form> {
        return formHazelcastRepository.get(formMapKey).flatMap { fe ->
            subject.checkAuthorization("forms:read:${fe.owner}:${fe.tenant}").map {
                fe
            }
        }
    }

    fun getFormByName(name: String, tenantId: TenantId, subject: Subject? = null): Single<Form>{
        return formHazelcastRepository.get(predicate =
                Predicates.and(
                        Predicates.equal<String, Form>("tenant", tenantId),
                        Predicates.equal<String, Form>("name", name)
                )
        )
    }

    fun getFormIdByName(name: String, tenantId: TenantId): Single<FormId>{
       return Single.fromCallable {
           formHazelcastRepository.mapService.project(
                   Projections.singleAttribute<MutableMap.MutableEntry<String, Form>, FormId>("id"),
                   Predicates.and(
                           Predicates.equal<String, Form>("tenant", tenantId),
                           Predicates.equal<String, Form>("name", name)
                   )
           )
       }.map {
           it.single()
       }
    }



    fun exists(formMapKey: FormId, mustExist: Boolean = false): Single<Boolean> {
        return formHazelcastRepository.exists(formMapKey).map {
            if (mustExist) {
                require(it, lazyMessage = { "Form does not exist." })
            }
            it
        }
    }

    fun existsByName(formName: String, tenantId: TenantId, mustExist: Boolean = false): Single<Boolean> {
        return getFormIdByName(formName, tenantId).map {
            true
        }.onErrorResumeNext {
            if (it is NoSuchElementException){
                if (mustExist){
                    Single.error(it)
                } else {
                    Single.just(false)
                }
            } else {
                Single.error(it)
            }
        }
    }

    fun formSchemaExists(formSchemaMapKey: FormSchemaEntityId, mustExist: Boolean = false): Single<Boolean> {
        return formSchemaHazelcastRepository.exists(formSchemaMapKey).map {
            if (mustExist) {
                require(it, lazyMessage = { "Form Schema does not exist." })
            }
            it
        }
    }

    /**
     * Update/overwrite Form
     * @param form Form to be updated/overwritten
     */
    fun updateForm(form: Form, subject: Subject? = null): Single<Form> {
        return formHazelcastRepository.update(form) { originalItem, newItem ->
            subject.checkAuthorization("forms:update:${originalItem.owner}:${originalItem.tenant}")
                    .subscribeOn(Schedulers.io()).blockingGet()

            //Update logic for automated fields @TODO consider automation with annotations
            newItem.copy(
                    ol = originalItem.ol + 1,
                    id = originalItem.id,
                    createdAt = originalItem.createdAt,
                    updatedAt = Instant.now()
            )
        }
    }

    /**
     * Update/overwrite Form Schema
     * @param formSchemaEntity Form Schema to be updated/overwritten
     */
    fun updateFormSchema(schema: FormSchema, isDefault: Boolean = false, subject: Subject? = null): Single<FormSchema> {
        return getForm(schema.formId, subject).map { fe ->
            if (isDefault) {
                // Check if they are allowed to update the Form with the Default key.
                // They have the permission to update the Schema, but not the permission to make it the default form.
                subject.checkAuthorization("forms:update:${fe.owner}:${fe.tenant}")
                        .subscribeOn(Schedulers.io()).blockingGet()
            }
           fe
        }.flatMap{ fe ->
            subject.checkAuthorization("form_schemas:update:${fe.owner}:${fe.tenant}").map {
                fe
            }
        }.flatMap {  fe ->
            formSchemaHazelcastRepository.update(schema) { originalItem, newItem ->
                // Update logic for automated fields @TODO consider automation with annotations
                newItem.copy(
                        ol = originalItem.ol + 1,
                        id = originalItem.id,
                        createdAt = originalItem.createdAt,
                        updatedAt = Instant.now()
                )
            }.map { fse ->
                Pair(fe, fse)
            }

        }.flatMap { (fe, fse) ->
            //@TODO consider adding a lock on the form entity incase of race condition between update of schema and update of form with default value.
            //If the form schema was created then update the Form with the default schema ID
            formHazelcastRepository.update(fe) { old, new ->
                old.copy(
                        ol = old.ol + 1,
                        defaultSchema = new.defaultSchema,
                        updatedAt = Instant.now()
                )

            }.map { res ->
                Pair(res, fse)
            }

        }.map { (_, fse) ->
            fse
        }
    }

    /**
     * Create/insert a form schema for a specific form id
     * @param formId Form ID
     * @param schema Form Schema
     * @param isDefault if the schema should be made the default schema for the Form.  This will make a update to the Form object.
     */
    fun createSchema(schema: FormSchema, isDefault: Boolean = false, subject: Subject? = null): Single<FormSchema> {
        return getForm(schema.formId).map { fe ->

            if (isDefault) {
                // Check if they are allowed to update the Form with the Default key.
                // They have the permission to update the Schema, but not the permission to make it the default form.
                subject.checkAuthorization("forms:update:${fe.owner}:${fe.tenant}")
                        .subscribeOn(Schedulers.io()).blockingGet()
            }
            fe
        }.flatMap { fe ->
            subject.checkAuthorization("form_schemas:create:${fe.owner}:${fe.tenant}").map {
                fe
            }
        }.flatMap { fe ->

            // Create the form schema
            formSchemaHazelcastRepository.create(schema).map { createdSchema ->
                Pair(fe, createdSchema)
            }

        }.flatMap { (fe, fse) ->
            //@TODO consider adding a lock on the form entity incase of race condition between update of schema and update of form with default value.
            //If the form schema was created then update the Form with the default schema ID
            formHazelcastRepository.update(fe) { old, new ->
                old.copy(
                        ol = old.ol + 1,
                        defaultSchema = new.defaultSchema,
                        updatedAt = Instant.now()
                )

            }.map { res ->
                Pair(res, fse)
            }

        }.map { (_, fse) ->
            fse
        }
    }

    /**
     * Get the default schema for the given form Id.
     * @param formId The Form Id
     */
    fun getDefaultSchema(formMapKey: FormId, subject: Subject? = null): Single<FormSchema> {
        // Requires that you can read the Form, and then Read the Schema that was referenced as the default schema in the Form.
        return getForm(formMapKey, subject).map { fe ->
            fe.defaultSchema ?: throw IllegalArgumentException("Default Schema is not set for form $formMapKey")

        }.flatMap { defaultSchemaUUID ->
            //@TODO this could be optimized to prevent getSchema from recalling getForm
            getSchema(defaultSchemaUUID, subject)
        }
    }

    fun getSchema(schemaMapKey: FormSchemaEntityId, subject: Subject? = null): Single<FormSchema> {
        return formSchemaHazelcastRepository.get(schemaMapKey)
                .onErrorResumeNext {
                    Single.error(IllegalArgumentException("Cannot find schema id"))
                }.flatMap { fse ->
                    // Does not use Subject because we use data from the Form to create the dynamic permission for the form schema
                    getForm(fse.formId).map { fe ->
                        Pair(fe, fse)
                    }
                }.flatMap { (fe, fse) ->
                    subject.checkAuthorization("form_schemas:read:${fe.owner}:${fe.tenant}").map {
                        fse
                    }
                }
    }

    /**
     * Get all schemas for the given form Id.
     * @param formId the Form Id that the Schemas belong to.
     */
    fun getAllSchemas(formMapKey: FormId, subject: Subject? = null, pageable: Pageable = Pageable.from(0)): Flowable<FormSchema> {
        return getForm(formMapKey).map { fe ->
            subject.checkAuthorization("form_schemas:read:${fe.owner}:${fe.tenant}")
        }.flatMapPublisher {
            formSchemaHazelcastRepository.getSchemasForForm(formMapKey, pageable)
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
    fun processFormSubmission(formMapKey: FormId, formSchemaMapKey: FormSchemaEntityId, formSubmission: FormSubmission, dryRun: Boolean = false, subject: Subject? = null): Single<FormSubmissionResponse> {
        // @TODO refactor to reduce amount of recalls to getSchema, getForm, etc, as Getting Schema also gets the Form.
        // Subject is not used for getSchema and getForm because we use this data to create the dynmaic permission
        return getSchema(formSchemaMapKey).flatMap { fse ->
            getForm(formMapKey).map { fe ->
                Pair(fe, fse)
            }
        }.flatMap { (fe, fse) ->
            subject.checkAuthorization("form_schemas:validate:${fe.owner}:${fe.tenant}").flatMap {
                submissionHandler.process(formSubmission, fe, fse, dryRun, subject)
            }
        }
    }
}