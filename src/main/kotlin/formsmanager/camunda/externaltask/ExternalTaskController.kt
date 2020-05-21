package formsmanager.camunda.externaltask

import io.micronaut.http.FullHttpRequest
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.server.netty.NettyHttpRequest
import io.netty.channel.ChannelFuture
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import org.camunda.bpm.engine.ProcessEngine

@Controller("/workflow/external-task")
@RequiresGuest
class ExternalTaskController(
        private val handler: ExternalTaskReactiveHandler,
        private val engine: ProcessEngine
) {


    private fun getNettyChannelCloseFuture(request: HttpRequest<*>): ChannelFuture {
        return when (request) {
            is FullHttpRequest<*> -> {
                (request.delegate as NettyHttpRequest<*>).channelHandlerContext.channel().closeFuture()
            }
            is NettyHttpRequest<*> -> {
                request.channelHandlerContext.channel().closeFuture()
            }
            else -> {
                throw IllegalStateException("Unexpected Netty request type.")
            }
        }
    }


    @Post("/fetchAndLock")
    fun fetchAndLock(request: HttpRequest<*>, @Body body: Single<HttpExternalTaskSubscription>): Single<HttpResponse<FetchAndLockResponse>> {
        return body.flatMapObservable { req ->
            req.closeFuture = getNettyChannelCloseFuture(request)
            handler.newSubscription(req)
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

    @Post("/start-instance-by-Key")
    fun startProcessInstanceByDefKey(request: HttpRequest<*>): Single<HttpResponse<String>> {
        return Single.fromCallable {
            engine.runtimeService.startProcessInstanceByKey("happy")
        }.map {
            HttpResponse.ok("processInstanceId: ${it.processInstanceId}")
        }
    }

}