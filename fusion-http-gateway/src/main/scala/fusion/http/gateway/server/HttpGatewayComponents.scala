package fusion.http.gateway.server

import akka.Done
import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Authority
import akka.http.scaladsl.model.Uri.Host
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import akka.http.scaladsl.server.Route
import fusion.core.extension.FusionCore
import fusion.core.util.Components
import helloscala.common.Configuration

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class HttpGatewayComponents(system: ExtendedActorSystem) extends Components[Route]("fusion.gateway.http") {
  implicit private def _system: ActorSystem = system
  override def configuration: Configuration = FusionCore(system).configuration

  override protected def createComponent(id: String): Route = {
    val gateway: GatewaySetting = GatewaySetting.fromActorSystem(system)
    logger.info("Gateway http配置：" + gateway)
    proxyAutoConfiguration(gateway)
  }

  private def proxyAutoConfiguration(gateway: GatewaySetting): Route = {
    val routes = gateway.locations.map { location =>
      val rawPath = if (location.location.head == '/') location.location.tail else location.location
      val path    = PathMatchers.separateOnSlashes(rawPath)
      pathPrefix(path) {
        extractRequest { request =>
          onComplete(proxyOnUpstream(request, location)) {
            case Success(response) => complete(response)
            case Failure(e)        => failWith(e)
          }
        }
      }
    }
    concat(routes: _*)
  }

  private def proxyOnUpstream(request: HttpRequest, location: GatewayLocation): Future[HttpResponse] = {
    import system.dispatcher

    val upstream = location.proxyUpstream
    val uri = location.proxyTo
      .map(proxyTo => Uri(proxyTo + request.uri.toString().drop(location.location.length)))
      .getOrElse(request.uri)

    val optionResolvedTargetF = for {
      serviceName <- upstream.serviceName
      discovery   <- upstream.discovery
    } yield discovery.lookup(serviceName, location.timeout).map(resolved => resolved.addresses.headOption)

    val resolvedTargetF = optionResolvedTargetF.getOrElse {
      val option = if (upstream.targets.isEmpty) {
        None
      } else {
        val i = Math.abs(location.proxyUpstream.targetsCounter.incrementAndGet() % upstream.targets.size)
        Some(upstream.targets(i))
      }
      Future.successful(option)
    }

    resolvedTargetF.flatMap {
      case Some(target) =>
        val targetUri =
          uri.copy(scheme = location.schema, authority = Authority(Host(target.host), target.port.getOrElse(0)))
        Http().singleRequest(request.copy(uri = targetUri))
      case _ =>
        Future.successful(HttpResponse(StatusCodes.BadGateway))
    }
  }

  override protected def componentClose(c: Route): Future[Done] = Future.successful(Done)
}
