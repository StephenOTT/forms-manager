package formsmanager.users.domain

import formsmanager.core.TenantField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.security.groups.domain.GroupEntity
import formsmanager.core.security.shiro.domain.Role
import formsmanager.users.UserMapKey
import formsmanager.users.repository.UserEntityWrapper
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class UserEntity(

        override val internalId: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val emailInfo: EmailInfo,

        val passwordInfo: PasswordInfo,

        val accountControlInfo: AccountControlInfo,

        val groupInfo: GroupsInfo,

        val rolesInfo: RolesInfo,

        override val tenant: UUID,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt

) : TimestampFields,
        TenantField,
        CrudableObject {

    companion object {

        const val USER_ROLE: String = "USER_ROLE"

        fun defaultUserRole(tenant: UUID, userInternalId: UUID): Role {
            return Role(USER_ROLE, setOf(
                    //@TODO move to configuration
                    "users:read,edit:${tenant}:${userInternalId}",
                    "groups:create,read,edit:${tenant}",
                    "groups:create:*"
            ))
        }

        fun defaultUserGroup(tenant: UUID, roles: Set<Role>): GroupEntity {
            return GroupEntity(
                    name = "User Group",
                    tenant = tenant,
                    roles = roles
            )
        }

        fun newUser(email: String, tenant: UUID, internalId: UUID = UUID.randomUUID()): UserEntity {
            return UserEntity(
                    internalId = internalId,
                    emailInfo = EmailInfo(email),
                    passwordInfo = PasswordInfo(resetPasswordInfo = ResetPasswordInfo()),
                    accountControlInfo = AccountControlInfo(),
                    tenant = tenant,
                    rolesInfo = RolesInfo(setOf(defaultUserRole(tenant, internalId))),
                    groupInfo = GroupsInfo(setOf(
                            defaultUserGroup(tenant,
                                    setOf(defaultUserRole(tenant, internalId))
                            )
                    ))
            )
        }
    }

    override fun toEntityWrapper(): UserEntityWrapper {
        return UserEntityWrapper(mapKey(), this::class.qualifiedName!!, this)
    }

    override fun mapKey(): UserMapKey {
        return UserMapKey(emailInfo.email, tenant)
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

    data class GroupsInfo(
            val groups: Set<GroupEntity> = setOf()
    )

    @Schema
    data class RolesInfo(
            val roles: Set<Role> = setOf(),

            /**
             * For future usage. Not yet implemented.
             */
            val customPermissions: Set<String> = setOf()
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