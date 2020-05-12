package formsmanager.core.security.login

import formsmanager.core.security.shiro.realm.LoginToken
import formsmanager.core.ifDebugEnabled
import formsmanager.users.service.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.Single
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.support.DefaultSubjectContext
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import javax.inject.Singleton


/**
 * Authenticates with Shiro.
 * The Shiro subject is put into a UserDetails Attribute map in the key "subject".
 * You can inject "Subject" Singleton throughout out the app or use UserDetails to get the subject through the attributes map.
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

        check(authenticationRequest.identity is LoginIdentity)
        check(authenticationRequest.secret is CharArray)

        val identity: LoginIdentity = authenticationRequest.identity as LoginIdentity
        val password: CharArray = authenticationRequest.secret as CharArray

        return userService.getByEmail(identity.username, identity.tenant)
                .onErrorResumeNext {
                    // @TODO review
                    // Could not find the email in users
                    Single.error(AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND), it))

                }.map { ue ->
                    if (!ue.emailConfirmed()) {
                        // User/Account has not been confirmed by email / The account is not setup yet, and therefore they do not have a password yet
                        throw AuthenticationFailureException(AuthenticationFailed(AuthenticationFailureReason.USER_DISABLED))

                    } else {
                        //Create subject
                        val subject = securityManager.createSubject(DefaultSubjectContext())

                        //Create LoginToken for Shiro
                        val loginToken = LoginToken(identity.username, password, identity.tenant)

                        // LOGIN with subject
                        subject.login(loginToken)

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
                        //Requires the cast for compiler to pick it up correctly
                        // Returns the UserDetails required by Micronaut
                        UserDetails(ue.id.toString(), null) as AuthenticationResponse
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
