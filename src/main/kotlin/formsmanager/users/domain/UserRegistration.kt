package formsmanager.users.domain

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

/**
 * Used for User Registration HTTP json body
 */
@Schema
data class UserRegistration(
        val email: String
)

/**
 * Response for a User Registration
 */
@Schema
data class UserRegistrationResponse(
        val status: String
)

/**
 * Used for Completing a User Registration as the Json Body for the /complete endpoint
 */
@Schema
data class CompleteRegistrationRequest(
        val userId: String,

        @JsonProperty("token1")
        val emailConfirmToken: UUID,

        @JsonProperty("token2")
        val pwdResetToken: UUID,

        @JsonProperty("password")
        var cleartextPassword: CharArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompleteRegistrationRequest

        if (userId != other.userId) return false
        if (emailConfirmToken != other.emailConfirmToken) return false
        if (pwdResetToken != other.pwdResetToken) return false
        if (!cleartextPassword.contentEquals(other.cleartextPassword)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + emailConfirmToken.hashCode()
        result = 31 * result + pwdResetToken.hashCode()
        result = 31 * result + cleartextPassword.contentHashCode()
        return result
    }
}