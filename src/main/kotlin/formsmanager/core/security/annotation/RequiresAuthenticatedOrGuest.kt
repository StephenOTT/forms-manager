package formsmanager.core.security.annotation


/**
 * Allows for Shrio to be Authenticated or allow a Guest/Anonymous user.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS,
        AnnotationTarget.CLASS,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAuthenticatedOrGuest