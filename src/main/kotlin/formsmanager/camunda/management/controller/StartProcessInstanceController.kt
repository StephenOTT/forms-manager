package formsmanager.camunda.management.controller

import com.hazelcast.core.HazelcastInstance
import formsmanager.camunda.hazelcast.variable.HazelcastVariable
import formsmanager.camunda.messagebuffer.service.MessageBufferService
import formsmanager.camunda.messagebuffer.MessageRequest
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
        private val hazelcastInstance: HazelcastInstance, // here for testing @TODO remove later
        private val messageBufferService: MessageBufferService,
        private val appCtx: ApplicationContext // here for testing @TODO remove later
) {

    @Post("/start")
    fun startProcessInstanceByDefKey(@Body body: StartProcessInstanceRequest): Single<HttpResponse<String>> {
        return Single.fromCallable {
            val variables = body.variables.mapValues {
                HazelcastVariable(it.key, it.value)
            }
//            engine.identityService.setAuthentication("123", null, listOf("someTenant"))
//            engine.runtimeService.createProcessInstanceByKey(body.key).businessKey(body.businessKey)
//                    .processDefinitionTenantId("someTenant")
            val process = engine.runtimeService.startProcessInstanceByKey(body.key, body.businessKey, variables)
//            engine.identityService.clearAuthentication()
            process
        }.map {
            HttpResponse.ok("processInstanceId: ${it.processInstanceId}")
        }
    }

    @Post("/message/correlate")
    fun correlateMessage(@Body body: MessageRequest): Single<HttpResponse<MessageCorrelationResponse>> {
        return messageBufferService.addToBuffer(body)
                .map {
                    HttpResponse.ok(MessageCorrelationResponse(it.id.asString()))
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