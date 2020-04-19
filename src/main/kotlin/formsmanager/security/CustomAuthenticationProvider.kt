package formsmanager.security

import formsmanager.ifDebugEnabled
import formsmanager.users.service.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.Single
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.support.DefaultSubjectContext
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import javax.inject.Singleton


//@TODO refactor Claims Generation for custom Issuer text
//@TODO refactor Rejection Handler for cleaner responses

/**
 * Used for converting micronaut auth reqest into a Shiro UsernamePasswordToken
 */
fun AuthenticationRequest<*, *>.toUsernamePasswordToken(): UsernamePasswordToken {
    return UsernamePasswordToken(this.identity.toString(), this.secret.toString())
}

/**
 * Authenticates with Shiro.
 * The Shiro subject is put into a UserDetails Attribute map in the key "subject".
 * You can inject "Subject" Singleton throughout out the app or use UserDetails to get the subject through the attributes map.
 * Subject is created on a RequestScope Singleton: meaning the subject is created and destroyed within the scope of the HTTP request.
 */
@Singleton
class CustomAuthenticationProvider(
        private val userService: UserService,
        private val securityManager: SecurityManager
) : AuthenticationProvider {

    companion object {
        private val log = LoggerFactory.getLogger(CustomAuthenticationProvider::class.java)
    }

    override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
        TODO("DEPRECATED")
    }

    override fun authenticate(request: HttpRequest<*>, authenticationRequest: AuthenticationRequest<*, *>): Publisher<AuthenticationResponse> {
        return userService.getUser(authenticationRequest.identity.toString())
                .onErrorResumeNext {
                    // @TODO review
                    // Could not find the email in users
                    Single.error(AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND), it))

                }.map { ue ->
                    if (!ue.emailConfirmed()) {
                        // User/Account has not been confirmed by email / The account is not setup yet, and therefore they do not have a password yet
                        throw AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.USER_DISABLED))

                    } else {
                        //LOGIN
                        val subject = securityManager.createSubject(DefaultSubjectContext())
                        subject.login(authenticationRequest.toUsernamePasswordToken())

                        if (subject.isAuthenticated) {
                            // Backup check to ensure that login was successful
                            log.ifDebugEnabled { "Auth Success: Password was valid." }
                            ue
                        } else {
                            // If we reach this point, then something went wrong...
                            log.error("Major Login Error: Auth Failure: Failure to Authenticate.  Failed login did not throw exception.")
                            throw AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH))
                        }
                    }

                }.map { ue ->
                    // Check if the account is active
                    if (!ue.accountActive()) {
                        log.ifDebugEnabled { "Auth Failure: Account is locked." }
                        throw AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.ACCOUNT_LOCKED))

                    } else {

                        // LOGIN SUCCESS:
                        UserDetails(ue.emailInfo.email, listOf()) as AuthenticationResponse //Requires the cast for compiler to pick it up correctly
                    }

                }.toFlowable().onErrorReturn {
                    //@TODO Review for refactor
                    when (it) {
                        is AuthenticationFailureException -> {
                            // Catching of the custom exceptions to trigger Micronaut responses
                            log.ifDebugEnabled { "Auth Failure: ${it.authFailure.reason.toString()}" }
                            it.authFailure

                        }
                        is AuthenticationException -> {
                            // Typically occurs when shiro's subject.login() failed
                            log.ifDebugEnabled { "Auth Failure: ${it.message}" }
                            AuthenticationFailed(it.message)

                        }
                        else -> {
                            // Unexpected error occurred
                            log.error(it.message, it)
                            AuthenticationFailed("Something went wrong")
                        }
                    }
                }
    }
}
