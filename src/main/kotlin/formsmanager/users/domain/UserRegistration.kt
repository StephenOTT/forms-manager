package formsmanager.users.domain

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.http.annotation.QueryValue
import java.util.*

data class UserRegistration(
        val email: String
)

data class UserRegistrationResponse(
        val status: String
)

data class CompleteRegistrationRequest(
        val id: UUID,

        val email: String,

        @JsonProperty("token1")
        val emailConfirmToken: UUID,

        @JsonProperty("token2")
        val pwdResetToken: UUID,

        @JsonProperty("pwd")
        var cleartextPassword: CharArray
) {

    fun destroyPwd(){
        this.cleartextPassword.forEachIndexed { index, _ ->
            this.cleartextPassword[index] = Char.MIN_VALUE
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompleteRegistrationRequest

        if (id != other.id) return false
        if (email != other.email) return false
        if (emailConfirmToken != other.emailConfirmToken) return false
        if (pwdResetToken != other.pwdResetToken) return false
        if (!cleartextPassword.contentEquals(other.cleartextPassword)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + emailConfirmToken.hashCode()
        result = 31 * result + pwdResetToken.hashCode()
        result = 31 * result + cleartextPassword.contentHashCode()
        return result
    }
}