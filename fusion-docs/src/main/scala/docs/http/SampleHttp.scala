package docs.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import fusion.http.FusionHttp

// #SampleHttp
object SampleHttp extends App with Directives {
  implicit val system = ActorSystem()

  val route = path("hello") {
    get {
      complete("Hello，Akka Fusion！")
    }
  }
  FusionHttp(system).startAwait(route)
}
// #SampleHttp
