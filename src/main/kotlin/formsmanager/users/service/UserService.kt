package formsmanager.users.service

import formsmanager.ifDebugEnabled
import formsmanager.security.shiro.PasswordService
import formsmanager.tenants.service.TenantService
import formsmanager.users.domain.UserEntity
import formsmanager.users.repository.UsersHazelcastRepository
import io.reactivex.Single
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton

@Singleton
class UserService(
        private val userRepository: UsersHazelcastRepository,
        private val tenantService: TenantService,
        private val passwordService: PasswordService
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }

    fun createUser(email: String, tenant: UUID, subject: Subject? = null): Single<UserEntity> {
        return Single.fromCallable {
            subject?.let {
                require(!it.isAuthenticated, lazyMessage = { "Cannot create user. Only anonymous users can create a user." })
            }

        }.flatMap {
            tenantService.tenantExists(tenant, null, true)

        }.flatMap {
            userRepository.userExists(email, tenant)

        }.map {
            if (it) {
                throw IllegalArgumentException("Unable to create user. Email already exists")
            } else {
                UserEntity.newUser(email, tenant)
            }

        }.doOnSuccess {
            log.ifDebugEnabled { "User Entity being Created: ${it}." }
        }.flatMap {
            userRepository.create(it)
        }
    }

    fun getUser(id: UUID, tenantId: UUID, subject: Subject? = null): Single<UserEntity> {
        return userRepository.find(id).map { ue ->
            require(ue.tenant == tenantId, lazyMessage = { "Cannot find user for tenant" })
            // Dynamic Permission, where the user MUST have access to their specific user ID.
            // Means that ever user will require a permission to access their account.
            subject?.let {
                subject.checkPermission(WildcardPermission("users:read:${ue.tenant}:${ue.id}"))
            }
            ue
        }
    }

    /**
     * @exception IllegalArgumentException if the email does not exist.
     */
    fun getUser(email: String, tenant: UUID, subject: Subject? = null): Single<UserEntity> {
        //@TODO add index to Users Map for the email property
        return userRepository.findByEmail(email, tenant).map { ue ->
            subject?.let {
                subject.checkPermission(WildcardPermission("users:read:${ue.tenant}:${ue.id}"))
            }
            ue
        }
    }

    fun resetPassword() {

    }

    /**
     * Determine if user exists by id.
     * Does not implement permission checks.
     */
    fun userIdExists(id: UUID): Single<Boolean> {
        return userRepository.exists(id)
    }

    /**
     * Determine if user exists by email and tenant id.
     * Does not implement permission checks.
     */
    fun userExists(email: String, tenant: UUID): Single<Boolean> {
        return userRepository.userExists(email, tenant)
    }

    /**
     * Updates the user entity.
     * Provides full control to update all fields in the user entity.
     * **Be careful when using this method.**
     * Tenant is provided as a method argument in scenarios where a user is being changed from one tenant to another.
     * If the user is being changed from one tenant to another, then the userEntity should contain the new tenant, and the method argument must have the current user's tenant
     */
    fun updateUser(userEntity: UserEntity, subject: Subject? = null): Single<UserEntity> {
        return Single.fromCallable {
            subject?.let {
                subject.checkPermission(WildcardPermission("users:update:${userEntity.tenant}:${userEntity.id}"))
            }
        }.flatMap {
            userRepository.update(userEntity) { originalItem, newItem ->
                //Update logic for automated fields @TODO consider automation with annotations
                newItem.copy(
                        ol = originalItem.ol + 1,
                        id = originalItem.id,
                        tenant = originalItem.tenant, //@TODO REVIEW // Means that a user cannot modify the tenant ID. / They cannot change a user's tenant.
                        createdAt = originalItem.createdAt,
                        updatedAt = Instant.now()
                )
            }
        }
    }

    fun lockUser() {

    }

    fun addRoles() {

    }

    fun removeRoles() {

    }

    fun addPermissions() {

    }

    fun removePermissions() {

    }

    fun sendEmailConfirmationMessage() {

    }

    fun sendPasswordResetMessage() {

    }

    /**
     * Checks if the user is active.
     * Does not have permission checks.
     */
    fun userIsActive(userId: UUID): Single<Boolean> {
        return userRepository.isActive(userId)
    }

    /**
     * Completes a user registration, with email verification and supply of password
     */
    fun completeRegistration(userId: UUID,
                             email: String,
                             tenantId: UUID,
                             emailConfirmToken: UUID,
                             pwdResetToken: UUID,
                             cleartextPassword: CharArray,
                             subject: Subject? = null
    ): Single<UserEntity> {
        return Single.fromCallable {
            subject?.let {
                require(!it.isAuthenticated, { "Unable to complete registration. Must be a anonymous user in order to complete a registration." })
            }
        }.flatMap {
            getUser(userId, tenantId).map {
                require(it.emailInfo.email == email) { "Invalid Email" }
                require(it.emailInfo.emailConfirmToken == emailConfirmToken) { "Invalid email token." }
                require(!it.emailInfo.emailConfirmed) { "Email is already confirmed." }
                check(it.passwordInfo.passwordHash == null) { "Password hash issue," }
                check(it.passwordInfo.salt == null) { "Password salt issue." }
                check(it.passwordInfo.resetPasswordInfo != null) { "Password reset issue." }
                require(it.passwordInfo.resetPasswordInfo.resetPasswordToken == pwdResetToken) { "Invalid password token." }
                it
            }
        }.flatMap { ue ->
            passwordService.hashPassword(cleartextPassword).map {
                ue.copy(
                        emailInfo = ue.emailInfo.copy(emailConfirmed = true),
                        passwordInfo = ue.passwordInfo.copy(
                                resetPasswordInfo = null,
                                passwordHash = it.toBase64(),
                                salt = it.salt.toBase64(),
                                algorithmName = it.algorithmName

                        )
                )
            }

        }.flatMap {
            updateUser(it)
        }
    }
}