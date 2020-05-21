package formsmanager.camunda.events

import io.reactivex.subjects.PublishSubject
import javax.inject.Singleton

@Singleton
class CamundaReactiveEvents {

    /**
     * Events are not thread-safe and **MUST** be treated at read-only
     * Events are not serialized to increase performance.
     */
    val externalTaskEvents = PublishSubject.create<ExternalTaskEvent>()

}