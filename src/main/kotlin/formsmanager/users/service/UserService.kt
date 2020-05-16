package formsmanager.users.service

import com.hazelcast.projection.Projections
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import formsmanager.core.ifDebugEnabled
import formsmanager.core.security.shiro.checkAuthorization
import formsmanager.core.security.shiro.credentials.PasswordService
import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.service.TenantService
import formsmanager.users.domain.User
import formsmanager.users.domain.UserId
import formsmanager.users.repository.UsersHazelcastRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.crypto.hash.Hash
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

    /**
     * Only Anonymous Users or if Subject == null can use these function
     */
    fun create(item: User, subject: Subject? = null): Single<User> {
        subject?.let {
            require(!it.isAuthenticated, lazyMessage = { "Cannot create user. Only anonymous users can create a user." })
        }
        return tenantService.exists(item.tenant, true)
                .flatMap {
                    userRepository.exists(item.id)

                }.map {
                    if (it) {
                        throw IllegalArgumentException("Unable to create user. User Already exists.")
                    } else {
                        item
                    }

                }.doOnSuccess {
                    log.ifDebugEnabled { "User Entity being Created: ${it}." }
                }.flatMap {
                    userRepository.create(it)
                }
    }

    fun get(userMapKey: UserId, subject: Subject? = null): Single<User> {
        return userRepository.get(userMapKey).flatMap { ue ->
            // Dynamic Permission, where the user MUST have access to their specific user ID.
            // Means that ever user will require a permission to access their account.
            subject.checkAuthorization("users:read:${ue.tenant}:${ue.id}").map {
                ue
            }
        }
    }

    fun getByUsername(username: String, tenantId: TenantId, subject: Subject? = null): Single<User> {
        return userRepository.get(Predicate {
            it.value.tenant == tenantId && it.value.username == username
        }).flatMap { user ->
            subject.checkAuthorization("users:read:${user.tenant}:${user.id}").map {
                user
            }
        }
    }

    fun resetPassword() {

    }


    fun exists(userMapKey: UserId): Single<Boolean> {
        return userRepository.exists(userMapKey)
    }

    fun getUserIdByEmail(email: String, tenantId: TenantId): Single<UserId> {
        return Single.fromCallable {
            userRepository.mapService.project(
                    Projections.singleAttribute<MutableMap.MutableEntry<String, User>, UserId>("id"),
                    Predicates.and(
                            Predicates.equal<String, User>("tenant", tenantId),
                            Predicates.equal<String, User>("emailInfo.email", email)
                    )
            ).single()
        }
    }


    /**
     * Updates the user entity.
     * Provides full control to update all fields in the user entity.
     * **Be careful when using this method.**
     * Tenant is provided as a method argument in scenarios where a user is being changed from one tenant to another.
     * If the user is being changed from one tenant to another, then the userEntity should contain the new tenant, and the method argument must have the current user's tenant
     */
    fun update(user: User, subject: Subject? = null): Single<User> {
        return userRepository.update(user) { originalItem, newItem ->
            subject.checkAuthorization("users:update:${user.tenant}:${user.id}")
                    .subscribeOn(Schedulers.io()).blockingGet()

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


    fun userIsActive(userMapKey: UserId): Single<Boolean> {
        return userRepository.isActive(userMapKey)
    }

    /**
     * Completes a user registration, with email verification and supply of password
     */
    fun completeRegistration(userId: UserId,
                             emailConfirmToken: UUID,
                             pwdResetToken: UUID,
                             cleartextPassword: CharArray,
                             subject: Subject? = null
    ): Single<User> {
        subject?.let {
            require(!it.isAuthenticated, { "Unable to complete registration. Must be a anonymous user in order to complete a registration." })
        }
        return get(userId).map {
//            require(it.emailInfo.email == userMapKey.email) { "Invalid Email" }
            require(it.emailInfo.emailConfirmToken == emailConfirmToken) { "Invalid email token." }
            require(!it.emailInfo.emailConfirmed) { "Email is already confirmed." }
            check(it.passwordInfo.passwordHash == null) { "Password hash issue," }
            check(it.passwordInfo.salt == null) { "Password salt issue." }
            check(it.passwordInfo.resetPasswordInfo != null) { "Password reset issue." }
            require(it.passwordInfo.resetPasswordInfo.resetPasswordToken == pwdResetToken) { "Invalid password token." }
            it

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
            update(it)
        }
    }

    fun createPasswordHash(password: CharArray): Single<Hash>{
        return passwordService.hashPassword(password)
    }

}