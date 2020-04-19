package formsmanager.users.service

import formsmanager.ifDebugEnabled
import formsmanager.security.PasswordService
import formsmanager.users.domain.UserEntity
import formsmanager.users.repository.UsersHazelcastRepository
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton

@Singleton
class UserService(
        private val userRepository: UsersHazelcastRepository,
        private val passwordService: PasswordService
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }

    fun createUser(email: String): Single<UserEntity> {
        return userRepository.userExists(email).map {
            if (it) {
                throw IllegalArgumentException("Unable to create user.  Email already exists")
            } else {
                UserEntity.newUser(email)
            }
        }.doOnSuccess {
            log.ifDebugEnabled { "User Entity being Created: $it" }
        }.flatMap {
            userRepository.create(it)
        }
    }

    fun getUser(id: UUID): Single<UserEntity> {
        return userRepository.find(id)
    }

    /**
     * @exception IllegalArgumentException if the email does not exist.
     */
    fun getUser(email: String): Single<UserEntity> {
        //@TODO add index to Users Map for the email property
        return userRepository.findByEmail(email)
    }

    fun resetPassword() {

    }

    fun userIdExists(id: UUID): Single<Boolean> {
        return userRepository.exists(id)
    }

    fun userExists(email: String): Single<Boolean> {
        return userRepository.userExists(email)
    }

    fun updateUser(userEntity: UserEntity): Single<UserEntity> {
        return userRepository.update(userEntity) { originalItem, newItem ->
            //Update logic for automated fields @TODO consider automation with annotations
            newItem.copy(
                    ol = originalItem.ol + 1,
                    id = originalItem.id,
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

    fun userIsActive(userId: UUID): Single<Boolean> {
        return userRepository.isActive(userId)
    }

    /**
     * Completes a user registration, with email verification and supply of password
     */
    fun completeRegistration(userId: UUID, email: String, emailConfirmToken: UUID, pwdResetToken: UUID, cleartextPassword: CharArray): Single<UserEntity> {
        return getUser(userId).map {
            require(it.emailInfo.email == email) { "Invalid Email" }
            require(it.emailInfo.emailConfirmToken == emailConfirmToken) { "Invalid email token." }
            require(!it.emailInfo.emailConfirmed) { "Email is already confirmed." }
            check(it.passwordInfo.passwordHash == null) { "Password hash issue," }
            check(it.passwordInfo.salt == null) { "Password salt issue." }
            check(it.passwordInfo.resetPasswordInfo != null) { "Password reset issue." }
            require(it.passwordInfo.resetPasswordInfo.resetPasswordToken == pwdResetToken) { "Invalid password token." }
            it

        }.flatMap {ue ->
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