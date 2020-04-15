package formsmanager.security

import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Replaces
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.security.authentication.*
import io.micronaut.security.token.config.TokenConfiguration
import io.micronaut.security.token.jwt.generator.claims.ClaimsAudienceProvider
import io.micronaut.security.token.jwt.generator.claims.JWTClaimsSetGenerator
import io.micronaut.security.token.jwt.generator.claims.JwtIdGenerator
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import javax.inject.Singleton


//class EmailUserDetails : UserDetails {
//    var email: String? = null
//
//    constructor(username: String?, roles: Collection<String?>?) : super(username, roles) {}
//    constructor(username: String?, roles: Collection<String?>?, email: String?) : super(username, roles) {
//        this.email = email
//    }
//
//}
//
@Singleton
class CustomAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>): Publisher<AuthenticationResponse> {
        require(authenticationRequest.identity == "sherlock") {
            AuthenticationFailed("Bad Username")
        }
        require(authenticationRequest.secret == "holmes") {
            AuthenticationFailed("Bad secret")
        }

        return Flowable.just(UserDetails("sherlock", listOf()))
    }
}

//
//@Singleton
//@Replaces(bean = JWTClaimsSetGenerator::class)
//class CustomJWTClaimsSetGenerator(tokenConfiguration: TokenConfiguration?,
//                                  @Nullable jwtIdGenerator: JwtIdGenerator?,
//                                  @Nullable claimsAudienceProvider: ClaimsAudienceProvider?,
//                                  @Nullable applicationConfiguration: ApplicationConfiguration?) : JWTClaimsSetGenerator(tokenConfiguration, jwtIdGenerator, claimsAudienceProvider, applicationConfiguration) {
//    override fun populateWithUserDetails(builder: JWTClaimsSet.Builder, userDetails: UserDetails) {
//        super.populateWithUserDetails(builder, userDetails)
//        if (userDetails is EmailUserDetails) {
//            builder.claim("email", userDetails.email)
//        }
//    }
//}
