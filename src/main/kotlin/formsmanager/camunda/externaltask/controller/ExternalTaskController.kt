package formsmanager.camunda.externaltask.controller

import formsmanager.camunda.externaltask.handler.http.HttpExternalTaskReactiveHandler
import formsmanager.camunda.externaltask.subscription.HttpExternalTaskSubscription
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.reactivex.Observable
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import org.camunda.bpm.engine.ProcessEngine
import java.util.concurrent.TimeUnit

@Controller("/workflow/external-task")
@RequiresGuest
class ExternalTaskController(
        private val handler: HttpExternalTaskReactiveHandler,
        private val engine: ProcessEngine
) {

    @Post("/fetchAndLock")
    fun fetchAndLock(@Body body: Single<HttpExternalTaskSubscription>): Single<HttpResponse<FetchAndLockResponse>> {
        return body.flatMapObservable { req ->
            handler.newSubscription(req)
                    .timeout(req.asyncResponseTimeout,
                            TimeUnit.MILLISECONDS,
                            Observable.just(FetchAndLockResponse(emptyList())))
        }.firstOrError().map {
            HttpResponse.ok(it)
        }
    }

//    @Post("/{id}/complete")
//    fun complete() {
//
//    }
//
//    @Post("/{id}/bpmnError")
//    fun handleBpmnError() {
//
//    }
//
//    @Post("/{id}/failure")
//    fun handleFailure() {
//
//    }
//
//    @Post("/{id}/errorDetails")
//    fun errorDetails() {
//
//    }
//
//    @Post("/{id}/extendLock")
//    fun extendLock() {
//
//    }
//
//    @Post("/{id}/unlock")
//    fun unlock() {
//
//    }
//
//    @Post("/{id}/priority")
//    fun setPriority() {
//
//    }
//
//    @Put("/{id}/retries")
//    fun setRetries() {
//
//    }
//
//    @Put("/retries")
//    fun setRetriesByQuery() {
//
//    }

}