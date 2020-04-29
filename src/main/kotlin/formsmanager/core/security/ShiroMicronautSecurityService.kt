package formsmanager.core.security

import formsmanager.ifDebugEnabled
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.support.DefaultSubjectContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * Provides a singleton to access the current Anonymous or Authenticated Shiro Subject when you are outside of a Request.
 * Typically you should be using a Subject argument in a controller (using the ShiroSubjectArgumentBinder).
 */
@Singleton
class ShiroMicronautSecurityService(
        private val securityManager: SecurityManager
) {

    companion object {
        const val SHIRO_SUBJECT = "shiro_subject"
    }

    private val log = LoggerFactory.getLogger(ShiroMicronautSecurityService::class.java)

    /**
     * Gets current subject from the provided request or creates a new request
     * Often used in cases such as a SecurityRule where the HttpRequest is not available yet to the singleton, but it available to the filter
     */
    fun getSubjectFromHttpRequest(httpRequest: HttpRequest<*>): Subject{
        log.ifDebugEnabled { "getSubjectFromHttpRequest() has been called." }

        val attr = httpRequest.getAttribute(SHIRO_SUBJECT, Subject::class.java)

        return if (!attr.isPresent){
            log.ifDebugEnabled { "Shiro Subject was not found in HTTP Request Attributes." }
            val anonymousSubject = createAnonymousSubject()

            httpRequest.setAttribute(SHIRO_SUBJECT, anonymousSubject)
            log.ifDebugEnabled { "Anonymous Shiro Subject created in Request Attribute: ${SHIRO_SUBJECT}." }

            anonymousSubject

        } else {
            val subject = attr.get()
            log.ifDebugEnabled { "Shiro Subject ${subject.principal} was found in HTTP Request Attributes." }
            subject
        }
    }

    /**
     * Returns the current Anonymous or Authenticated Subject.
     * Make sure you know what you are doing when using this.
     * should only be used when you are unable to get the subject from the request (because it was not provided) or
     * you were not able to pass the Subject from the request from a super.
     */
    fun getSubject(): Subject {
        log.ifDebugEnabled { "getSubject() has been called." }
        val auth: HttpRequest<Any> = ServerRequestContext.currentRequest<Any>().get()
        return getSubjectFromHttpRequest(auth)
    }

    fun createAnonymousSubject(): Subject{
        log.ifDebugEnabled { "Creating a Anonymous Shiro Subject" }
        return securityManager.createSubject(DefaultSubjectContext())
    }
}