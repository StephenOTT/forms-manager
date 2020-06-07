package formsmanager.camunda.management.controller

import formsmanager.camunda.engine.message.CamundaMessageBuffer
import formsmanager.camunda.engine.message.MessageRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import org.apache.shiro.authz.annotation.RequiresGuest

@Controller("/workflow/message")
@RequiresGuest
class CorrelateMessageController(
        private val messageBuffer: CamundaMessageBuffer
) {

    @Post("/correlate")
    fun correlateMessage(@Body body: MessageRequest): Single<HttpResponse<MessageCorrelationResponse>> {
        // @TODO add permission handling
        return messageBuffer.insert(body)
                .map {
                    HttpResponse.ok(MessageCorrelationResponse(it.id.toMapKey()))
                }
    }

}