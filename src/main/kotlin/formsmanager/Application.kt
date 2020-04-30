package formsmanager

import com.hazelcast.query.impl.predicates.*
import formsmanager.core.hazelcast.query.sql.checkPredicateRules
import io.micronaut.context.annotation.Context
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import javax.annotation.PostConstruct

@OpenAPIDefinition(
        info = Info(
                title = "Forms Manager",
                version = "1.0",
                description = "Forms Manager API",
                contact = Contact(url = "http://github.com/stephenott", name = "StephenOTT")
        )
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("formsmanager")
                .mainClass(Application.javaClass)
                .start()
    }
}


@Context
class ToBeDeleted {
    @PostConstruct
    fun initialize() {

        val pred2 = SqlPredicate("kids[any].alive=true OR age <= 20 AND name LIKE 'cat' OR dog=false AND frank > 100")

        pred2.checkPredicateRules()
    }
}
