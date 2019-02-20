package fusion.http.gateway

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import fusion.http.HttpSourceQueue
import fusion.http.util.HttpUtils

object GatewayComponent {
  val queues = new ConcurrentHashMap[String, HttpSourceQueue]()

  /**
   *
   * @param path "fusion.http.gateway.target-uri"
   * @param config [[Config]]
   * @return
   */
  def proxyRoute(
      path: String = "fusion.http.gateway.target-uri",
      config: Config = ConfigFactory.load()
  )(implicit system: ActorSystem, mat: Materializer): Route = {
    import mat.executionContext
    extractRequestContext { ctx =>
      val uri = config.getString(path)
      implicit val queue = queues.computeIfAbsent(uri, _ => HttpUtils.cachedHostConnectionPool(uri))
      val request = ctx.request.copy(uri = uri)
      onSuccess(HttpUtils.hostRequest(request)) { response =>
        complete(response)
      }
    }
  }

}
