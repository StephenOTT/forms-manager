package formsmanager.camunda.externaltask.controller

import io.micronaut.context.annotation.Prototype
import io.micronaut.runtime.http.scope.RequestScope
import java.util.concurrent.CompletableFuture
import javax.annotation.PreDestroy

@Prototype
@RequestScope
class HttpRequestLifecycle(){

    val requestFuture = CompletableFuture<Unit>()

    @PreDestroy
    fun destroy(){
        requestFuture.complete(Unit)
    }
}