package formsmanager.validator

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.jackson.annotation.JacksonFeatures
import io.reactivex.Single

@Client("\${formValidator.client.host}")
@JacksonFeatures(
        enabledDeserializationFeatures = [
            DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
            DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
        ]
)
interface FormValidatorClient {

    @Post("/validate")
    fun validate(@Body validationRequest: FormSubmission): Single<HttpResponse<ValidationResponseValid>>

}

interface ValidationResponse{
    //@TODO Add jsonType support
}

data class ValidationResponseValid(val processed_submission: Map<String, Any?>): ValidationResponse

data class ValidationResponseInvalid(@get:JsonProperty("isJoi") @param:JsonProperty("isJoi") val isJoi: Boolean,
                                     val name: String,
                                     val details: List<Map<String, Any>>,
                                     val _object: Map<String, Any>,
                                     val _validated: Map<String, Any>): ValidationResponse

class FormValidationException(val responseBody: ValidationResponseInvalid) : RuntimeException("Form Validation Exception") {}