package formsmanager.exception

import io.micronaut.http.HttpStatus
import java.lang.RuntimeException

open class FormManagerException(override val message: String,
                                cause: Throwable? = null,
                                val info: Any? = null,
                                val httpStatus: HttpStatus?) : RuntimeException(message, cause) {
    fun toErrorMessage(): ErrorMessage {
        return ErrorMessage(this.message, this.info)
    }
}

data class ErrorMessage(val message: String, val info: Any?)

class NotFoundException(message: String, cause: Throwable? = null, info: Any? = null, httpStatus: HttpStatus = HttpStatus.NOT_FOUND) : FormManagerException(message, cause, info, httpStatus)
class CrudOperationException(message: String, cause: Throwable? = null, info: Any? = null, httpStatus: HttpStatus? = HttpStatus.BAD_REQUEST) : FormManagerException(message, cause, info, httpStatus)
class AlreadyExistsException(message: String, cause: Throwable? = null, info: Any? = null, httpStatus: HttpStatus? = HttpStatus.CONFLICT) : FormManagerException(message, cause, info, httpStatus)
class OptimisticLockingException(message: String, cause: Throwable? = null, info: Any? = null, httpStatus: HttpStatus? = HttpStatus.CONFLICT) : FormManagerException(message, cause, info, httpStatus)
class SomethingWentWrongException(message: String = "Oh Oh...Something went wrong.", cause: Throwable? = null, info: Any? = null, httpStatus: HttpStatus? = HttpStatus.INTERNAL_SERVER_ERROR) : FormManagerException(message, cause, info, httpStatus)
