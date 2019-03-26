package fusion.actuator

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.actuator.route.FusionActuatorRoute
import fusion.actuator.setting.ActuatorSetting
import fusion.core.extension.FusionExtension
import helloscala.common.Configuration

final class FusionActuator private (override protected val _system: ExtendedActorSystem)
    extends FusionExtension
    with StrictLogging {
  val actuatorSetting = ActuatorSetting(Configuration(system.settings.config))
  def route: Route    = new FusionActuatorRoute(_system, actuatorSetting).route
}

object FusionActuator extends ExtensionId[FusionActuator] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionActuator = new FusionActuator(system)
  override def lookup(): ExtensionId[_ <: Extension]                        = FusionActuator
}
