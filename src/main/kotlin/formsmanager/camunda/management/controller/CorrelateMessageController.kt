package formsmanager.camunda.management.controller

import formsmanager.camunda.messagebuffer.service.MessageBufferService
import formsmanager.camunda.messagebuffer.MessageRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest

@Controller("/workflow/message")
@RequiresGuest
class CorrelateMessageController(
        private val messageBufferService: MessageBufferService
) {

    @Post("/correlate")
    fun correlateMessage(@Body body: MessageRequest): Single<HttpResponse<MessageCorrelationResponse>> {
        // @TODO add permission handling
        return messageBufferService.addToBuffer(body)
                .map {
                    HttpResponse.ok(MessageCorrelationResponse(it.id.asString()))
                }
    }

}