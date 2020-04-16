package formsmanager.security

import io.micronaut.security.authentication.AuthenticationFailed

class AuthenticationFailureException(val authFailure: AuthenticationFailed,
                                     underlyingException: Throwable? = null) : RuntimeException(authFailure.reason.toString(), underlyingException)