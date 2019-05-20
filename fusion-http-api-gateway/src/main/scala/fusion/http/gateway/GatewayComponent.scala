package fusion.http.gateway

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Authority
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import fusion.http.HttpSourceQueue
import fusion.http.util.HttpUtils

object GatewayComponent {
  val queues = new ConcurrentHashMap[Authority, HttpSourceQueue]()

//  /**
//   *
//   * @param path "fusion.http.gateway.target-uri"
//   * @param config [[Config]]
//   * @return
//   */
//  def proxyRoute(path: String = "fusion.http.gateway.target-uri", config: Config = ConfigFactory.load())(
//      implicit system: ActorSystem,
//      mat: Materializer): Route = {
//    extractRequestContext { ctx =>
//      val uri     = config.getString(path)
//      val queue   = queues.computeIfAbsent(uri, _ => HttpUtils.cachedHostConnectionPool(uri))
//      val request = ctx.request.copy(uri = uri)
//      onSuccess(HttpUtils.hostRequest(request)(queue, mat.executionContext)) { response =>
//        complete(response)
//      }
//    }
//  }

  def proxyRoute(targetBaseUri: Uri)(implicit system: ActorSystem, mat: Materializer): Route =
    proxyRoute(targetBaseUri, identity)

  def proxyRoute(targetBaseUri: Uri, uriMapping: Uri => Uri)(implicit system: ActorSystem, mat: Materializer): Route = {
    extractRequestContext { ctx =>
      val queue =
        queues.computeIfAbsent(targetBaseUri.authority, _ => HttpUtils.cachedHostConnectionPool(targetBaseUri))
      val uri     = ctx.request.uri.copy(scheme = targetBaseUri.scheme, authority = targetBaseUri.authority)
      val request = ctx.request.copy(uri = uriMapping(uri))
      onSuccess(HttpUtils.hostRequest(request)(queue, mat.executionContext)) { response =>
        complete(response)
      }
    }
  }

}
