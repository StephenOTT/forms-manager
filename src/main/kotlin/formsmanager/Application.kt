package formsmanager

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info

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
