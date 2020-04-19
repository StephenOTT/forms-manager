package formsmanager.controller

import formsmanager.domain.*
import formsmanager.exception.ErrorMessage
import formsmanager.exception.FormManagerException
import formsmanager.exception.NotFoundException
import formsmanager.service.FormService
import formsmanager.submission.FormSubmissionResponse
import formsmanager.validator.FormSubmission
import formsmanager.validator.FormSubmissionData
import formsmanager.validator.FormValidationException
import formsmanager.validator.ValidationResponseInvalid
import io.micronaut.context.annotation.Requires
import io.micronaut.core.bind.ArgumentBinder.BindingResult
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder
import io.micronaut.http.filter.OncePerRequestHttpServerFilter
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.filters.SecurityFilter
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Single
import org.apache.shiro.subject.Subject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton


@Controller("/form")
@Secured(SecurityRule.IS_AUTHENTICATED)
class FormManagerController(
        private val formService: FormService
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
    fun getForm(uuid: UUID): Single<HttpResponse<FormEntity>> {
        return formService.getForm(uuid)
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
    fun createForm(subject: Subject, @Body form: FormEntityCreator): Single<HttpResponse<FormEntity>> {
        return formService.createForm(form.toFormEntity(UUID.randomUUID()))
                .map {
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
    fun updateForm(subject: Subject, uuid: UUID, @Body form: FormEntityCreator): Single<HttpResponse<FormEntity>> {
        return formService.updateForm(form.toFormEntity(uuid))
                .map {
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
    fun getSchema(subject: Subject, uuid: UUID, schemaUuid: UUID): Single<HttpResponse<FormSchemaEntity>> {
        return formService.formExists(uuid).flatMap {
            if (it) {
                formService.getSchema(schemaUuid)
            } else {
                throw IllegalArgumentException("Cannot find form ${uuid}")
            }
        }.map {
            HttpResponse.ok(it)
        }
    }

    @Get("/{uuid}/schema/{schemaUuid}/data")
    fun getSchemaData(subject: Subject, uuid: UUID, schemaUuid: UUID): Single<HttpResponse<FormSchema>> {
        return formService.formExists(uuid).flatMap {
            if (it) {
                formService.getSchema(schemaUuid)
            } else {
                throw IllegalArgumentException("Cannot find form ${uuid}")
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
    fun createSchema(subject: Subject, uuid: UUID, @Body schemaEntity: FormSchemaEntityCreator, @QueryValue isDefault: Boolean?): Single<HttpResponse<FormSchemaEntity>> {
        return formService.createSchema(uuid, schemaEntity.toFormSchemaEntity(UUID.randomUUID(), uuid), isDefault)
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
    @Patch("/{uuid}/schema/{schemaUuid}{?isDefault}")
    fun updateSchema(subject: Subject, uuid: UUID, schemaUuid: UUID, @Body formSchema: FormSchemaEntityCreator, @QueryValue isDefault: Boolean?): Single<HttpResponse<FormSchemaEntity>> {
        return formService.updateFormSchema(uuid, formSchema.toFormSchemaEntity(schemaUuid, uuid), isDefault)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Get the default Schema for a specific Form
     * @param uuid The Id of the Form
     * @return The Form Schema that was marked as Default for the specific Form.
     */
    @Get("/{uuid}/schema")
    fun getFormDefaultSchema(subject: Subject, uuid: UUID): Single<HttpResponse<FormSchemaEntity>> {
        return formService.getDefaultSchema(uuid).map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Get all Form Schemas for a specific Form
     * @param uuid The Id of the Form
     * @return A array of Form Schema Entities
     */
    @Get("/{uuid}/schemas")
    fun getAllSchemas(subject: Subject, uuid: UUID): Single<HttpResponse<List<FormSchemaEntity>>> {
        return formService.getAllSchemas(uuid)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Validate a submission against a specific schema uuid for a specific form uuid.
     * Form UUID must exist.
     * @param uuid The Id of the Form
     * @param schemaUuid The Id of the Form Schema
     * @param submission Form Submission Data
     * @return validation response
     */
    @Post("/{uuid}/schemas/{schemaUuid}/validate")
    fun validateFormsSpecificSchema(subject: Subject, uuid: UUID, schemaUuid: UUID, @Body submission: Single<FormSubmissionData>): Single<HttpResponse<Map<String, Any?>>> {
        return formService.formExists(uuid).flatMap {
            if (it) {
                formService.getSchema(schemaUuid)
            } else {
                Single.error(IllegalArgumentException("Cannot find form ${uuid}"))
            }
        }.flatMap {
            submission.map { submissionData -> FormSubmission(it.schema, submissionData) }
        }.flatMap {
            formService.validationFormSubmissionAsTask(it)
        }.map {
            HttpResponse.ok(it.processed_submission)
        }
    }

    /**
     * Validate a submission against a schema based on the Form's Default Schema.
     * A Form must have a default schema applied.
     * @param uuid The Id of the Form
     * @param submission Form Submission Data
     * @return validation response
     */
    @Post("/{uuid}/validate")
    fun validateDefaultSchema(subject: Subject, uuid: UUID, @Body submission: Single<FormSubmissionData>): Single<HttpResponse<Map<String, Any?>>> {
        return formService.getDefaultSchema(uuid).flatMap { schema ->
            submission.map { submissionData ->
                FormSubmission(schema.schema, submissionData)
            }
        }.flatMap {
            formService.validationFormSubmissionAsTask(it)
        }.map {
            HttpResponse.ok(it.processed_submission)
        }
    }

    /**
     * Raw validation endpoint allowing you to provide a custom schema and submission
     * @param submission Form Submission
     * @return validation response
     */
    @Post("/validate")
    fun validate(subject: Subject, @Body submission: Single<FormSubmission>): Single<HttpResponse<Map<String, Any?>>> {
        return submission.flatMap {
            formService.validationFormSubmissionAsTask(it)
        }.map {
            HttpResponse.ok(it.processed_submission)
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
    @Post("/{uuid}/schemas/{schemaUuid}/submit")
    fun submitFormsSpecificSchema(subject: Subject, uuid: UUID, schemaUuid: UUID, @Body submission: Single<FormSubmissionData>): Single<HttpResponse<FormSubmissionResponse>> {
        return formService.formExists(uuid).flatMap {
            if (it) {
                formService.getSchema(schemaUuid)
            } else {
                Single.error(IllegalArgumentException("Cannot find form ${uuid}"))
            }
        }.flatMap {
            submission.map { submissionData -> FormSubmission(it.schema, submissionData) }
        }.flatMap {
            formService.processFormSubmission(it)

        }.map {
            HttpResponse.ok(it)
        }
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