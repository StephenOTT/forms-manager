package formsmanager.controller

import formsmanager.domain.FormEntity
import formsmanager.domain.FormSchemaEntity
import formsmanager.exception.ErrorMessage
import formsmanager.exception.FormManagerException
import formsmanager.service.FormService
import formsmanager.validator.FormSubmission
import formsmanager.validator.FormSubmissionData
import formsmanager.validator.FormValidationException
import formsmanager.validator.ValidationResponseInvalid
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.*

@Controller("/form")
class FormManagerController(
        private val formService: FormService
) {

    private val log: Logger = LoggerFactory.getLogger(FormManagerController::class.java)

    @Post("/")
    fun create(@Body formEntity: FormEntity): Single<HttpResponse<FormEntity>> {
        return formService.createForm(formEntity)
                .map {
                    HttpResponse.ok(it)
                }
    }

    @Get("/{uuid}")
    fun getForm(uuid: UUID): Single<HttpResponse<FormEntity>> {
        return formService.getForm(uuid)
                .map {
                    HttpResponse.ok(it)
                }
    }

    @Patch("/")
    fun update(@Body formEntity: FormEntity): Single<HttpResponse<FormEntity>> {
        return formService.updateForm(formEntity)
                .map {
                    HttpResponse.ok(it)
                }
    }

    @Post("/{uuid}/schema{?isDefault}")
    fun createSchema(uuid: UUID, @Body schemaEntity: FormSchemaEntity, @QueryValue isDefault: Boolean?): Single<HttpResponse<FormSchemaEntity>> {
        return formService.createSchema(uuid, schemaEntity, isDefault)
                .map {
                    HttpResponse.ok(it)
                }
    }

    @Get("/{uuid}/schema")
    fun getDefaultSchema(uuid: UUID): Single<HttpResponse<FormSchemaEntity>> {
        return formService.getDefaultSchema(uuid).map {
            HttpResponse.ok(it)
        }
    }

    @Get("/{uuid}/schemas")
    fun getAllSchemas(uuid: UUID): Single<HttpResponse<List<FormSchemaEntity>>> {
        return formService.getAllSchemas(uuid)
                .map {
                    HttpResponse.ok(it)
                }
    }

    /**
     * Validate a submission against a specific schema uuid for a specific form uuid.
     * Form UUID must exist.
     */
    @Post("/{uuid}/schemas/{schemaUuid}/validate")
    fun validateFormsSpecificSchema(uuid: UUID, schemaUuid: UUID, @Body submission: Single<FormSubmissionData>): Single<HttpResponse<Map<String, Any?>>> {
        return formService.formExists(uuid).flatMap {
            if (it) {
                formService.getSchema(schemaUuid)
            } else {
                throw IllegalArgumentException("Cannot find form ${uuid}")
            }
        }.flatMap {
            submission.map { submissionData ->
                FormSubmission(it.schema, submissionData)
            }
        }.flatMap {
            formService.validateFormSubmission(it)
        }.map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Validate a submission against a schema based on the Form's Default Schema.
     * A Form must have a default schema applied.
     */
    @Post("/{uuid}/validate")
    fun validateDefaultSchema(uuid: UUID, @Body submission: Single<FormSubmissionData>): Single<HttpResponse<Map<String, Any?>>> {
        return formService.getDefaultSchema(uuid).flatMap { schema ->
            submission.map { submissionData ->
                FormSubmission(schema.schema, submissionData)
            }
        }.flatMap {
            formService.validateFormSubmission(it)
        }.map {
            HttpResponse.ok(it)
        }
    }

    /**
     * Raw validation endpoint allowing you to provide a custom schema and submission
     */
    @Post("/validate")
    fun validate(@Body submission: Single<FormSubmission>): Single<HttpResponse<Map<String, Any?>>> {
        return submission.flatMap {
            formService.validateFormSubmission(it)
        }.map {
            HttpResponse.ok(it)
        }
    }

    @Error
    fun formValidationError(request: HttpRequest<*>, exception: FormValidationException): HttpResponse<ValidationResponseInvalid> {
        return HttpResponse.badRequest(exception.responseBody)
    }

    @Error
    fun commonError(request: HttpRequest<*>, exception: FormManagerException): HttpResponse<ErrorMessage> {
        log.error(exception.message, exception)

        return HttpResponse.status<ErrorMessage>(exception.httpStatus).body(exception.toErrorMessage())
    }
}