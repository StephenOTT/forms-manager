package formsmanager.security

import formsmanager.ifDebugEnabled
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.shiro.authc.credential.DefaultPasswordService
import org.apache.shiro.crypto.hash.DefaultHashService
import org.apache.shiro.crypto.hash.Hash
import org.apache.shiro.crypto.hash.Sha512Hash
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class PasswordService {

    companion object {
        //@TODO setup config
        //@TODO add optional ini setup?

        const val hashIterationsCount: Int = 500000 //500,000
        const val hashAlgorithmName: String = Sha512Hash.ALGORITHM_NAME

        private val log = LoggerFactory.getLogger(PasswordService::class.java)
    }

    private val passwordService = DefaultPasswordService()

    init {
        // Set the Hash Algorithm
        (passwordService.hashService as DefaultHashService).hashAlgorithmName = hashAlgorithmName
    }

    /**
     * Hashes cleartext password.
     * Subscribes on RxJava Computation Scheduler
     */
    fun hashPassword(cleartextPassword: CharArray): Single<Hash> {
        return Single.fromCallable {
            log.ifDebugEnabled { "Generating Hashed Password." }
            // Bytesource is already done by the hashPassword...
            passwordService.hashPassword(cleartextPassword)
        }.subscribeOn(Schedulers.computation())
    }
}