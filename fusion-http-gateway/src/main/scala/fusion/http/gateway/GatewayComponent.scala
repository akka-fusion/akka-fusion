package fusion.http.gateway

import java.util.concurrent.ConcurrentHashMap

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Authority
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.Config
import fusion.core.http.HttpSourceQueue
import fusion.http.util.HttpUtils

object GatewayComponent {
  val queues = new ConcurrentHashMap[Authority, HttpSourceQueue]()

  /**
   * @param config 全局配置 [[com.typesafe.config.Config]]
   * @param path "fusion.http.gateway.target-uri"
   * @return
   */
  def proxyRouteFromPath(config: Config, path: String = "fusion.http.gateway.target-uri"): Route =
    proxyRoute(Uri(config.getString(path)))

  def proxyRoute(targetBaseUri: Uri): Route =
    proxyRoute(targetBaseUri, identity)

  def proxyRoute(targetBaseUri: Uri, uriMapping: Uri => Uri): Route = {
    extractRequestContext { ctx =>
      extractActorSystem { implicit system =>
        import ctx.materializer
        val queue =
          queues.computeIfAbsent(targetBaseUri.authority, _ => HttpUtils.cachedHostConnectionPool(targetBaseUri))
        val uri       = ctx.request.uri.copy(scheme = targetBaseUri.scheme, authority = targetBaseUri.authority)
        val request   = ctx.request.copy(uri = uriMapping(uri))
        val responseF = HttpUtils.hostRequest(request)(queue, ctx.materializer.executionContext)
        onSuccess(responseF) { response =>
          complete(response)
        }
      }
    }
  }

}
