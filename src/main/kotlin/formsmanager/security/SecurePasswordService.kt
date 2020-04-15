package formsmanager.security

import formsmanager.ifDebugEnabled
import io.reactivex.Single
import org.apache.shiro.codec.Hex
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.util.ByteSource
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class SecurePasswordService {

    companion object {
        const val hashIterations: Int = 250000

        private val log = LoggerFactory.getLogger(SecurePasswordService::class.java)

    }

    fun generateSalt(): ByteSource {
        log.ifDebugEnabled { "Salt Generated." }
        return SecureRandomNumberGenerator().nextBytes()
    }

    fun hashPassword(cleartextPassword: CharArray, salt: ByteSource): Single<String> {
        return Single.fromCallable {
            log.ifDebugEnabled { "Generating Hashed Password." }
            Sha512Hash(cleartextPassword, salt, hashIterations).toHex()
        }
    }

    fun passwordMatchesSource(sourceHashedPassword: String, sourceSalt: String, cleartextPassword: CharArray): Single<Boolean>{
        return Single.fromCallable {
            log.ifDebugEnabled { "comparing source and provided password hashes." }
            ByteSource.Util.bytes(Hex.decode(sourceSalt))
        }.flatMap { salt ->
            hashPassword(cleartextPassword, salt)
        }.map {
            it == sourceHashedPassword
        }
    }

}