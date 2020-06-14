package formsmanager.camunda.management.controller

import com.hazelcast.core.HazelcastInstance
import formsmanager.camunda.engine.message.CamundaMessageBuffer
import formsmanager.camunda.engine.message.MessageRequest
import formsmanager.camunda.engine.variable.HazelcastVariable
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest
import org.camunda.bpm.engine.ProcessEngine

@Controller("/workflow/process-definition")
@RequiresGuest
class StartProcessInstanceController(
        private val engine: ProcessEngine,
        private val hazelcastInstance: HazelcastInstance,
        private val messageBuffer: CamundaMessageBuffer,
        private val appCtx: ApplicationContext
) {

    @Post("/start")
    fun startProcessInstanceByDefKey(@Body body: StartProcessInstanceRequest): Single<HttpResponse<String>> {
        return Single.fromCallable {
            val variables = body.variables.mapValues {
                HazelcastVariable(it.key, it.value)
            }
            engine.identityService.setAuthentication("123", null, listOf("someTenant"))
            val process = engine.runtimeService.startProcessInstanceByKey(body.key, body.businessKey, variables)
            engine.identityService.clearAuthentication()
            process
        }.map {
            HttpResponse.ok("processInstanceId: ${it.processInstanceId}")
        }
    }

    @Post("/message/correlate")
    fun correlateMessage(@Body body: MessageRequest): Single<HttpResponse<MessageCorrelationResponse>> {
        return messageBuffer.insert(body)
                .map {
                    HttpResponse.ok(MessageCorrelationResponse(it.id.toMapKey()))
                }
    }

    @Get("/test")
    fun test(): HttpResponse<String> {
        println("casts")
//        engine.identityService.setAuthentication("123", null, listOf("someTenant"))
//        engine.runtimeService.createProcessInstanceQuery().list()
        return HttpResponse.ok("done")
    }
}