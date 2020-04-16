package formsmanager.users.domain

import formsmanager.domain.*
import formsmanager.hazelcast.map.CrudableObject
import formsmanager.users.repository.UserEntityWrapper
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class UserEntity(

        override val id: UUID = UUID.randomUUID(),
        override val ol: Long = 0,

        val emailInfo: EmailInfo,

        val passwordInfo: PasswordInfo,

        val accountControlInfo: AccountControlInfo,

        val rolesInfo: RolesInfo,

        override val tenant: String? = null,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt

): TimestampFields,
        TenantField,
        CrudableObject<UUID> {

    companion object {

        const val USER_ROLE: String = "user_role"

        fun newUser(email: String, tenant: String? = null): UserEntity{
            return UserEntity(
                    emailInfo = EmailInfo(email),
                    passwordInfo = PasswordInfo(resetPasswordInfo = ResetPasswordInfo()),
                    accountControlInfo = AccountControlInfo(),
                    rolesInfo = RolesInfo(setOf(USER_ROLE)),
                    tenant = tenant
            )
        }
    }

    override fun toEntityWrapper(): UserEntityWrapper {
        return UserEntityWrapper(id, this::class.qualifiedName!!, this)
    }

    data class EmailInfo(
            val email: String,
            val emailConfirmed: Boolean = false,
            val emailConfirmToken: UUID = UUID.randomUUID()
            //@TODO add logging for when email confirmation occurred
    )

    data class PasswordInfo(
            val passwordHash: String? = null,
            val salt: String? = null,
            val resetPasswordInfo: ResetPasswordInfo? = null
    )

    data class ResetPasswordInfo(
            val resetPasswordToken: UUID = UUID.randomUUID(),
            val resetPasswordTokenGeneratedAt: Instant = Instant.now()
    )

    data class AccountControlInfo(
            val locked: Boolean = false,
            val lockedAt: Instant? = null,
            val accountAccessFailCount: Int = 0
    )

    data class RolesInfo(
            val roles: Set<String> = setOf()
    )


    fun accountActive(): Boolean {
        return kotlin.runCatching {
            require(!this.accountControlInfo.locked) { "Account is locked" }
            require(this.emailInfo.emailConfirmed) { "Email is not confirmed" }
        }.onFailure {
            throw it
        }.isSuccess
    }

}