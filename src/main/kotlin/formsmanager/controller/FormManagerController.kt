package formsmanager.controller

import formsmanager.domain.*
import formsmanager.exception.ErrorMessage
import formsmanager.exception.FormManagerException
import formsmanager.exception.NotFoundException
import formsmanager.hazelcast.query.checkAllowedSortProperties
import formsmanager.service.FormService
import formsmanager.submission.FormSubmissionResponse
import formsmanager.tenants.service.TenantService
import formsmanager.validator.FormSubmission
import formsmanager.validator.FormSubmissionData
import formsmanager.validator.FormValidationException
import formsmanager.validator.ValidationResponseInvalid
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


@Controller("/form/{tenant}")
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
    @Get("/{uuid}")
    fun getForm(subject: Subject,
                @QueryValue tenant: UUID,
                @QueryValue uuid: UUID
    ): Single<HttpResponse<FormEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.getForm(uuid, tenant, subject)
        }.map {
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
                   @QueryValue tenant: UUID,
                   @Body form: FormEntityCreator
    ): Single<HttpResponse<FormEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.createForm(
                    form.toFormEntity(UUID.randomUUID(), tenant),
                    tenant,
                    subject
            )
        }.map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Update a Form
     * @param uuid The Id of the form to be updated.
     * @param form The updated form.
     * @return The Form
     */
    @Patch("/{uuid}")
    fun updateForm(subject: Subject,
                   @QueryValue tenant: UUID,
                   @QueryValue uuid: UUID,
                   @Body form: FormEntityCreator
    ): Single<HttpResponse<FormEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.updateForm(form.toFormEntity(uuid, tenant), tenant, subject)
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
    @Get("/{uuid}/schema/{schemaUuid}")
    fun getSchema(subject: Subject,
                  @QueryValue tenant: UUID,
                  @QueryValue uuid: UUID,
                  @QueryValue schemaUuid: UUID
    ): Single<HttpResponse<FormSchemaEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.formExists(uuid)
        }.flatMap {
            if (it) {
                formService.getSchema(schemaUuid, tenant, subject)
            } else {
                Single.error(IllegalArgumentException("Cannot find form $uuid"))
            }
        }.map {
            HttpResponse.ok(it)
        }
    }

    @Get("/{uuid}/schema/{schemaUuid}/data")
    fun getSchemaData(subject: Subject,
                      @QueryValue tenant: UUID,
                      @QueryValue uuid: UUID,
                      @QueryValue schemaUuid: UUID
    ): Single<HttpResponse<FormSchema>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.formExists(uuid)
        }.flatMap {
            if (it) {
                formService.getSchema(schemaUuid, tenant, subject)
            } else {
                Single.error(IllegalArgumentException("Cannot find form $uuid"))
            }
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
    @Post("/{uuid}/schema{?isDefault}")
    fun createSchema(subject: Subject,
                     @QueryValue uuid: UUID,
                     @QueryValue tenant: UUID,
                     @Body schemaEntity: FormSchemaEntityCreator,
                     @QueryValue isDefault: Boolean?
    ): Single<HttpResponse<FormSchemaEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.createSchema(uuid, schemaEntity.toFormSchemaEntity(UUID.randomUUID(), uuid), isDefault
                    ?: false, tenant, subject)
        }.map {
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
    @Patch("/{uuid}/schema/{schemaUuid}{?isDefault}")
    fun updateSchema(subject: Subject,
                     @QueryValue tenant: UUID,
                     @QueryValue uuid: UUID,
                     @QueryValue schemaUuid: UUID,
                     @Body formSchema: FormSchemaEntityCreator,
                     @QueryValue isDefault: Boolean?
    ): Single<HttpResponse<FormSchemaEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.updateFormSchema(
                    uuid,
                    formSchema.toFormSchemaEntity(schemaUuid, uuid),
                    isDefault ?: false,
                    tenant,
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
    @Get("/{uuid}/schema")
    fun getFormDefaultSchema(subject: Subject,
                             @QueryValue tenant: UUID,
                             @QueryValue uuid: UUID
    ): Single<HttpResponse<FormSchemaEntity>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.getDefaultSchema(uuid, tenant, subject)
        }.map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Get all Form Schemas for a specific Form
     * @param uuid The Id of the Form
     * @return A array of Form Schema Entities
     */
    @Get("/{uuid}/schemas")
    fun getAllSchemas(subject: Subject,
                      @QueryValue tenant: UUID,
                      @QueryValue uuid: UUID,
                      pageable: Pageable
    ): Single<HttpResponse<List<FormSchemaEntity>>> {
        return tenantService.tenantExists(tenant, subject, true).map {
            pageable.checkAllowedSortProperties(listOf("updatedAt")) //@TODO consider moving this into the service layer
        }.flatMap {
            formService.getAllSchemas(uuid, tenant, subject, pageable).toList()
        }.map {
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
    @Post("/{uuid}/schemas/{schemaUuid}/submit{?dryRun}")
    fun submitFormsSpecificSchema(subject: Subject,
                                  @QueryValue tenant: UUID,
                                  @QueryValue uuid: UUID,
                                  @QueryValue schemaUuid: UUID,
                                  @Body submission: Single<FormSubmissionData>,
                                  @QueryValue dryRun: Boolean?
    ): Single<HttpResponse<FormSubmissionResponse>> {
        return tenantService.tenantExists(tenant, subject, true).flatMap {
            formService.formExists(uuid)
        }.flatMap {
            if (it) {
                formService.getSchema(schemaUuid, tenant)
            } else {
                Single.error(IllegalArgumentException("Cannot find form $uuid"))
            }
        }.flatMap {
            submission.map { submissionData ->
                FormSubmission(it.schema, submissionData)
            }
        }.flatMap {
            formService.processFormSubmission(schemaUuid, tenant, it, dryRun ?: false, subject)

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