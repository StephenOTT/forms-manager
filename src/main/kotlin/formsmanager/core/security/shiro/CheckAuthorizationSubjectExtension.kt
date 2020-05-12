package formsmanager.core.security.shiro

import io.reactivex.Single
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.subject.Subject

fun Subject?.checkAuthorization(vararg permissions: String, logical: Logical = Logical.AND): Single<Unit> {
    return if (this != null) {
        Single.fromCallable {
            val resultSet = this.isPermitted(*permissions)

            val result = when (logical) {
                Logical.AND -> {
                    resultSet.all { it }

                }
                Logical.OR -> {
                    resultSet.any { it }

                }
                else -> {
                    throw IllegalStateException("Unexpected Logical value in annotaiton configuration")
                }
            }

            if (result) {
                // If all good then
                Unit
            } else {
                throw AuthorizationException("Subject ${this.principal} is not permitted for the requested action.")
            }

        }
    } else {
        return Single.just(Unit)
    }
}