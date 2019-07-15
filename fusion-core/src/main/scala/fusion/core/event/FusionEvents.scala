package fusion.core.event

import com.typesafe.scalalogging.StrictLogging
import fusion.core.event.http.HttpBindingServerEvent

class FusionEvents extends StrictLogging {
  val http: EventListenerRegistry[HttpBindingServerEvent] = new EventListenerRegistry[HttpBindingServerEvent]() {}
}
