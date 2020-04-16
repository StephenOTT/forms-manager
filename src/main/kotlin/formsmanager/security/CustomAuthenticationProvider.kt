package formsmanager.security

import formsmanager.ifDebugEnabled
import formsmanager.users.service.UserService
import io.micronaut.security.authentication.*
import io.reactivex.Single
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import javax.inject.Singleton


//@TODO refactor Claims Generation for custom Issuer text
//@TODO refactor Rejection Handler for cleaner responses

@Singleton
class CustomAuthenticationProvider(
        private val userService: UserService,
        private val pwdService: SecurePasswordService

) : AuthenticationProvider {

    companion object {
        private val log = LoggerFactory.getLogger(CustomAuthenticationProvider::class.java)
    }

    override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>): Publisher<AuthenticationResponse> {
        return userService.getUser(authenticationRequest.identity.toString())
                .onErrorResumeNext {
                    Single.error(AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND), it))
                }.map { ue ->
                    // Check if the account is active
                    if (!ue.accountActive()) {
                        log.ifDebugEnabled { "Auth Failure: Account is locked." }
                        throw AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.ACCOUNT_LOCKED))
                    } else {
                        ue
                    }

                }.flatMap { ue ->
                    // Validate the Password
                    check(ue.passwordInfo.passwordHash != null)
                    check(ue.passwordInfo.salt != null)
                    pwdService.passwordMatchesSource(
                            ue.passwordInfo.passwordHash,
                            ue.passwordInfo.salt,
                            authenticationRequest.secret.toString().toCharArray()).map {
                        Pair(it, ue)
                    }

                }.map { (isPwdValid, ue) ->
                    // If password is valid then return UserDetails
                    if (isPwdValid) {
                        log.ifDebugEnabled { "Auth Success: Password was valid." }
                        UserDetails(ue.id.toString(), ue.rolesInfo.roles) as AuthenticationResponse
                    } else {
                        // If the password is incorrect then return auth failed
                        log.ifDebugEnabled { "Auth Failure: Password was invalid." }
                        throw AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH))
                    }

                }.toFlowable()
                .onErrorReturn {
                    if (it is AuthenticationFailureException) {
                        log.ifDebugEnabled { "Auth Failure: ${it.authFailure}" }
                        it.authFailure
                    } else {
                        log.error(it.message, it)
                        AuthenticationFailed("Something went wrong")
                    }
                }
    }
}
