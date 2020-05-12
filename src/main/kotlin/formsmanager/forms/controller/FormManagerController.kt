package formsmanager.forms.controller

import formsmanager.core.exception.ErrorMessage
import formsmanager.core.exception.FormManagerException
import formsmanager.core.exception.NotFoundException
import formsmanager.core.hazelcast.query.checkAllowedSortProperties
import formsmanager.forms.domain.*
import formsmanager.forms.service.FormService
import formsmanager.forms.submission.FormSubmissionResponse
import formsmanager.tenants.service.TenantService
import formsmanager.forms.validator.FormSubmission
import formsmanager.forms.validator.FormSubmissionData
import formsmanager.forms.validator.FormValidationException
import formsmanager.forms.validator.ValidationResponseInvalid
import formsmanager.tenants.domain.TenantId
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.subject.Subject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


@Controller("/form/{tenantName}")
//@RequiresAuthentication
@RequiresGuest
class FormManagerController(
        private val formService: FormService,
        private val tenantService: TenantService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(FormManagerController::class.java)
    }

    /**
     * Get a form.
     * @param uuid the Id of the form.
     * @return The form.
     * @exception NotFoundException Could not find form based on Id.
     */
    @Get("/{formName}")
    fun getForm(subject: Subject,
                @QueryValue tenantName: TenantId,
                @QueryValue formName: String
    ): Single<HttpResponse<Form>> {
        return formService.getFormByName(formName, tenantName, subject)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Create a Form.
     * @param form The Form to be created.
     * @return the created Form
     */
    @Post("/")
    fun createForm(subject: Subject,
                   @QueryValue tenantName: TenantId,
                   @Body form: FormCreator
    ): Single<HttpResponse<Form>> {
        return formService.createForm(
                form.toForm(tenantId = tenantName),
                subject
        ).map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Update a Form
     * @param uuid The Id of the form to be updated.
     * @param form The updated form.
     * @return The Form
     */
    @Patch("/{formName}")
    fun updateForm(subject: Subject,
                   @QueryValue tenantName: TenantId,
                   @QueryValue formName: String,
                   @Body form: FormModifier
    ): Single<HttpResponse<Form>> {
        return formService.getFormByName(formName, tenantName)
                .flatMap { fe ->
                    formService.updateForm(form.toForm(fe.id, fe.tenant), subject)

                }.map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Get a Form's specific Schema
     * @param uuid The Id of the Form
     * @param schemaUuid The Id of the Form Schema
     * @return The Form Schema
     */
    @Get("/formName/schema/{schemaUuid}")
    fun getSchema(subject: Subject,
                  @QueryValue tenantName: TenantId,
                  @QueryValue formName: String,
                  @QueryValue schemaUuid: UUID
    ): Single<HttpResponse<FormSchema>> {
        return formService.existsByName(formName, tenantName, true)
                .flatMap {
                    formService.getSchema(FormSchemaEntityId(schemaUuid))
                }.map {
                    HttpResponse.ok(it)
                }
    }

    @Get("/{formName}/schema/{schemaUuid}/render")
    fun getSchemaForRender(subject: Subject,
                           @QueryValue tenantName: TenantId,
                           @QueryValue formName: String,
                           @QueryValue schemaUuid: UUID
    ): Single<HttpResponse<FormioFormSchema>> {
        return formService.existsByName(formName, tenantName, true)
                .flatMap {
                    formService.getSchema(FormSchemaEntityId(schemaUuid))
                }.map {
                    HttpResponse.ok(it.schema)
                }
    }


    /**
     * Create a form schema for a form
     * @param uuid The Id of the form
     * @param schemaEntity The form schema to create
     * @param isDefault if the schema should be marked as the default schema for the Form.
     * @return The Form Schema
     */
    @Post("/{formName}/schema{?isDefault}")
    fun createSchema(subject: Subject,
                     @QueryValue formName: String,
                     @QueryValue tenantName: TenantId,
                     @Body schemaEntity: FormSchemaCreator,
                     @QueryValue isDefault: Boolean?
    ): Single<HttpResponse<FormSchema>> {
        return formService.getFormIdByName(formName, tenantName)
                .flatMap {
                    formService.createSchema(schemaEntity.toFormSchema(formMapKey = it), isDefault
                            ?: false, subject)
                }
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Update a Form Schema for a specific Form
     * @param uuid The Id of the Form.
     * @param schemaUuid the Id of the Form Schema to update.
     * @param formSchema The updated Form Schema
     * @return The updated Form Schema
     */
    @Patch("/{formName}/schema/{schemaUuid}{?isDefault}")
    fun updateSchema(subject: Subject,
                     @QueryValue tenantName: TenantId,
                     @QueryValue formName: String,
                     @QueryValue schemaUuid: UUID,
                     @Body formSchema: FormSchemaModifier,
                     @QueryValue isDefault: Boolean?
    ): Single<HttpResponse<FormSchema>> {
        return formService.getSchema(FormSchemaEntityId(schemaUuid)).flatMap { fse ->
            formService.updateFormSchema(
                    formSchema.toFormSchema(fse.id, fse.formId),
                    isDefault ?: false,
                    subject
            )
        }.map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Get the default Schema for a specific Form
     * @param uuid The Id of the Form
     * @return The Form Schema that was marked as Default for the specific Form.
     */
    @Get("/{formName}/schema")
    fun getFormDefaultSchema(subject: Subject,
                             @QueryValue tenantName: TenantId,
                             @QueryValue formName: String
    ): Single<HttpResponse<FormSchema>> {
        return formService.getFormIdByName(formName, tenantName).flatMap {
            formService.getDefaultSchema(it, subject)
        }.map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Get all Form Schemas for a specific Form
     * @param uuid The Id of the Form
     * @return A array of Form Schema Entities
     */
    @Get("/{formName}/schemas")
    fun getAllSchemas(subject: Subject,
                      @QueryValue tenantName: TenantId,
                      @QueryValue formName: String,
                      pageable: Pageable
    ): Single<HttpResponse<List<FormSchema>>> {
        pageable.checkAllowedSortProperties(listOf("updatedAt")) //@TODO consider moving this into the service layer

        return formService.getFormIdByName(formName, tenantName).toFlowable()
                .flatMap {
                    formService.getAllSchemas(it, subject, pageable)
                }.toList()
                .map {
                    HttpResponse.ok(it)
                }


    }


    /**
     * Submit a Form submission against a specific schema uuid for a specific form uuid.
     * Uses the FormSubmissionHandler / Submission Strategy for processing of the Submission.
     * This endpoint will only validate form ID and schema ID; all other validations are the responsibility of the SubmissionHandler.
     * Form UUID must exist.
     * @param uuid The Id of the Form
     * @param schemaUuid The Id of the Form Schema
     * @param submission Form Submission Data
     * @return validation response
     */
    @Post("/{formName}/schemas/{schemaUuid}/submit{?dryRun}")
    fun submitFormsSpecificSchema(subject: Subject,
                                  @QueryValue tenantName: TenantId,
                                  @QueryValue formName: String,
                                  @QueryValue schemaUuid: UUID,
                                  @Body submission: Single<FormSubmissionData>,
                                  @QueryValue dryRun: Boolean?
    ): Single<HttpResponse<FormSubmissionResponse>> {
        return formService.getFormIdByName(formName, tenantName).flatMap {
            formService.getSchema(FormSchemaEntityId(schemaUuid))

        }.flatMap {
            submission.map { submissionData ->
                val sub = FormSubmission(it.schema, submissionData)
                Pair(sub, it)
            }
        }.flatMap { (sub, schemaEntity) ->
            formService.processFormSubmission(schemaEntity.formId, schemaEntity.id, sub, dryRun?: false, subject)

        }.map {
            HttpResponse.ok(it)
        }
    }


    @Error
    fun argumentError(request: HttpRequest<*>, exception: IllegalArgumentException): HttpResponse<String> {
        if (log.isDebugEnabled) {
            log.debug(exception.message, exception)
        }
        return HttpResponse.badRequest(exception.message)
    }

    @Error
    fun authzError(request: HttpRequest<*>, exception: AuthorizationException): HttpResponse<Unit> {
        log.error(exception.message, exception) //@TODO move to a Authorization Logger
        return HttpResponse.unauthorized()
    }

    @Error
    fun formValidationError(request: HttpRequest<*>, exception: FormValidationException): HttpResponse<ValidationResponseInvalid> {
        log.error(exception.message, exception)

        return HttpResponse.badRequest(exception.responseBody)
    }

    @Error
    fun commonError(request: HttpRequest<*>, exception: FormManagerException): HttpResponse<ErrorMessage> {
        log.error(exception.message, exception)

        return HttpResponse.status<ErrorMessage>(exception.httpStatus).body(exception.toErrorMessage())
    }
}