package docs

import akka.actor.ActorSystem
import fusion.actuator.FusionActuator
import fusion.discovery.client.nacos.FusionNacos
import fusion.http.FusionHttpServer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object FusionNacosDemo extends App {
  implicit val system = ActorSystem("fusion-nacos-demo")
  val actuatorRoute   = FusionActuator(system).route
  FusionHttpServer(system).component.startRouteSync(actuatorRoute)
  FusionNacos(system).component.namingService.registerInstance("hongka.file.converter", "192.168.1.53", 8000)

  StdIn.readLine()
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
