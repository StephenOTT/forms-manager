package formsmanager.users.domain

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.TenantField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.core.security.groups.domain.GroupId
import formsmanager.tenants.domain.TenantId
import formsmanager.users.repository.UserEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class UserId(override val value: UUID): CrudableObjectId<UserId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun type(): String {
        return "user"
    }

    override fun compareTo(other: UserId): Int {
        return value.compareTo(other.value)
    }
}

@Schema
data class User(

        override val id: UserId,

        override val ol: Long = 0,

        val username: String,

        val emailInfo: EmailInfo,

        val passwordInfo: PasswordInfo,

        val accountControlInfo: AccountControlInfo,

        val groupInfo: GroupsInfo,

        override val tenant: TenantId,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt

) : TimestampFields,
        TenantField,
        CrudableObject {

    companion object {

        fun newUser(email: String, tenant: TenantId, groups: Set<GroupId>, id: UserId = UserId(UuidUtil.newSecureUUID())): User {
            return User(
                    id = id,
                    username = email,
                    emailInfo = EmailInfo(email),
                    passwordInfo = PasswordInfo(resetPasswordInfo = ResetPasswordInfo()),
                    accountControlInfo = AccountControlInfo(),
                    tenant = tenant,
                    groupInfo = GroupsInfo(groups = groups)
            )
        }

        /**
         * Creates a confirmed user
         */
        fun newUser(email: String, passwordHash: String, salt: String, algorithmName: String, tenant: TenantId, groups: Set<GroupId>, id: UserId = UserId(UuidUtil.newSecureUUID())): User {
            return User(
                    id = id,
                    username = email,
                    emailInfo = EmailInfo(email = email, emailConfirmed = true),
                    passwordInfo = PasswordInfo(
                            passwordHash = passwordHash,
                            salt = salt,
                            algorithmName = algorithmName,
                            resetPasswordInfo = ResetPasswordInfo()
                    ),
                    accountControlInfo = AccountControlInfo(),
                    tenant = tenant,
                    groupInfo = GroupsInfo(groups = groups)
            )
        }

    }

    override fun toEntityWrapper(): UserEntity {
        return UserEntity(id, this::class.qualifiedName!!, this)
    }

    @Schema
    data class EmailInfo(
            val email: String,
            val emailConfirmed: Boolean = false,
            val emailConfirmToken: UUID = UUID.randomUUID()
            //@TODO add logging for when email confirmation occurred
    )

    @Schema
    data class PasswordInfo(
            val passwordHash: String? = null,
            val salt: String? = null,
            val algorithmName: String? = null,
            val resetPasswordInfo: ResetPasswordInfo? = null
    )

    @Schema
    data class ResetPasswordInfo(
            val resetPasswordToken: UUID = UUID.randomUUID(),
            val resetPasswordTokenGeneratedAt: Instant = Instant.now()
    )

    @Schema
    data class AccountControlInfo(
            val locked: Boolean = false,
            val lockedAt: Instant? = null,
            val accountAccessFailCount: Int = 0
    )

    @Schema
    data class GroupsInfo(
            val groups: Set<GroupId> = setOf()
    )

    fun emailConfirmed(): Boolean {
        return this.emailInfo.emailConfirmed
    }

    fun accountLocked(): Boolean {
        return this.accountControlInfo.locked
    }

    fun accountActive(): Boolean {
        return emailConfirmed() && !accountLocked()
    }

}