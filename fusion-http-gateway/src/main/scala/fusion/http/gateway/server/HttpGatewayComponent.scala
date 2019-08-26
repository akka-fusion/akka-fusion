/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.http.gateway.server

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.PathMatchers
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.stream.QueueOfferResult
import com.typesafe.scalalogging.StrictLogging
import fusion.http.HttpSourceQueue
import fusion.http.server.AbstractRoute
import fusion.http.util.HttpUtils
import helloscala.common.exception.HSBadGatewayException
import helloscala.common.exception.HSServiceUnavailableException
import helloscala.common.util.CollectionUtils

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

abstract class HttpGatewayComponent(id: String, system: ActorSystem) extends AbstractRoute with StrictLogging {

  implicit protected def _system: ActorSystem = system
  implicit protected val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher
  protected val httpSourceQueueMap = new ConcurrentHashMap[(String, Int), HttpSourceQueue]()
  protected val gateway: GatewaySetting = GatewaySetting.fromActorSystem(system, id)

  override def route: Route = {
    logger.info("Gateway http配置：" + gateway)
    proxyAutoConfiguration()
  }

  protected def proxyAutoConfiguration(): Route = {
    val routes = gateway.locations.map { location =>
      val rawPath = if (location.location.head == '/') location.location.tail else location.location
      val path = PathMatchers.separateOnSlashes(rawPath)
      pathPrefix(path) {
        mapRequestContext(ctx => transformRequestContext(location, ctx)) {
          extractRequest { request =>
            onComplete(proxyOnUpstream(request, location)) {
              case Success(response) => complete(response)
              case Failure(ex)       => failWith(ex)
            }
          }
        }
      }
    }
    concat(routes: _*)
  }

  protected def transformRequestContext(location: GatewayLocation, ctx: RequestContext): RequestContext = {
    ctx
      .withRoutingSettings(transformRoutingSettings(location, ctx.settings))
      .withRequest(clearHttpRequest(location, ctx.request))
  }

  protected def clearHttpRequest(location: GatewayLocation, request: HttpRequest): HttpRequest = {
    val req = location.routingSettings.sizeLimit match {
      case Some(maxBytes) if maxBytes < 1L => request.withEntity(request.entity.withoutSizeLimit())
      case Some(maxBytes)                  => request.withEntity(request.entity.withSizeLimit(maxBytes))
      case _                               => request
    }

    val notProxyHeaders = location.notProxyHeaders ++ gateway.notProxyHeaders
    req.copy(headers = req.headers.filterNot(h => notProxyHeaders(h.lowercaseName())))
  }

  protected def transformRoutingSettings(location: GatewayLocation, settings: RoutingSettings): RoutingSettings = {
    var s = settings
    location.routingSettings.decodeMaxBytesPerChunk.foreach(n => s = s.withDecodeMaxBytesPerChunk(n))
    location.routingSettings.decodeMaxSize.foreach(n => s = s.withDecodeMaxSize(n))
    location.routingSettings.fileGetConditional.foreach(b => s = s.withFileGetConditional(b))
    location.routingSettings.rangeCoalescingThreshold.foreach(n => s = s.withRangeCoalescingThreshold(n))
    location.routingSettings.rangeCountLimit.foreach(n => s = s.withRangeCountLimit(n))
    location.routingSettings.renderVanityFooter.foreach(b => s = s.withRenderVanityFooter(b))
    location.routingSettings.verboseErrorMessages.foreach(b => s = s.withVerboseErrorMessages(b))
    s
  }

  protected def proxyOnUpstream(req: HttpRequest, location: GatewayLocation): Future[HttpResponse] = {
    import system.dispatcher
    try {
      val uri = location.proxyToUri(req.uri)
      val requestF = findRealRequest(req, location, uri)
      requestF
        .flatMap { request =>
          val responseF = executeRequest(request)
          val circuitF = location.circuitBreaker.map(_.withCircuitBreaker(responseF)).getOrElse(responseF)
          circuitF.recover {
            case e: TimeoutException =>
              logger.error(s"proxy to upstream timeout：${location.upstream} $req", e)
              HttpResponse(StatusCodes.GatewayTimeout, entity = HttpEntity(e.toString))
            case e =>
              logger.error(s"proxy to upstream error：${location.upstream} $req", e)
              HttpResponse(StatusCodes.ServiceUnavailable, entity = HttpEntity(e.toString))
          }
        }
        .recover {
          case e =>
            val msg = s"服务地址无效；${location.upstream}"
            logger.error(msg, e)
            HttpUtils.jsonResponse(StatusCodes.BadGateway, msg)
        }
    } catch {
      case e: Throwable =>
        Future.successful(
          HttpResponse(
            StatusCodes.ServiceUnavailable,
            entity = HttpUtils.entityJson(StatusCodes.ServiceUnavailable, e.getMessage)))
    }
  }

  protected def findRealRequest(req: HttpRequest, location: GatewayLocation, uri: Uri): Future[HttpRequest] = {
    val upstream: GatewayUpstream = location.upstream
    val hostAndPortF = if (CollectionUtils.nonEmpty(upstream.targets)) {
      val i = Math.abs(location.upstream.targetsCounter.incrementAndGet() % upstream.targets.size)
      val target = upstream.targets(i)
      logger.trace(s"static target: $target")
      val targetUri = uri.withAuthority(target.host, target.port.getOrElse(HttpUtils.DEFAULT_PORTS(uri.scheme)))
      Future.successful(req.withUri(targetUri))
    } else {
      val serviceName =
        upstream.serviceName.getOrElse(throw HSBadGatewayException(s"upstream.serviceName未指定：${upstream}"))
      upstream.discovery
        .getOrElse(throw HSBadGatewayException(s"upstream.serviceName未指定：${upstream}"))
        .lookup(serviceName, 10.seconds)
        .map { resolved =>
          val target = resolved.addresses.headOption.getOrElse(throw HSBadGatewayException(s"后端服务不可用：$serviceName"))
          logger.trace(s"discovery health target: $target")
          val targetUri = uri.withAuthority(
            target.host,
            target.port.getOrElse(
              throw HSBadGatewayException(s"服务未指定端口号：$serviceName；[${target.host}:${target.port}]")))
          req.withUri(targetUri)
        }
    }
    hostAndPortF
  }

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。
   */
  def executeRequest(request: HttpRequest): Future[HttpResponse] = {
    val uri = request.uri
    val sourceQueueKey = Tuple2(uri.authority.host.address(), uri.effectivePort)
    val responsePromise = Promise[HttpResponse]()
    val queue = httpSourceQueueMap.computeIfAbsent(sourceQueueKey, _ => HttpUtils.cachedHostConnectionPool(uri, 512))
    val noAuthorityRequest = request.copy(uri = HttpUtils.clearAuthority(uri))
    queue
      .offer(noAuthorityRequest -> responsePromise)
      .flatMap {
        case QueueOfferResult.Enqueued => responsePromise.future
        case QueueOfferResult.Dropped =>
          Future.failed(HSServiceUnavailableException(s"Queue: $sourceQueueKey overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) =>
          httpSourceQueueMap.remove(sourceQueueKey)
          val badGatewayException = HSBadGatewayException(
            s"Queue: $sourceQueueKey exception: ${ex.getLocalizedMessage}. Try again later.",
            cause = ex)
          Future.failed(badGatewayException)
        case QueueOfferResult.QueueClosed =>
          httpSourceQueueMap.remove(sourceQueueKey)
          val msg = s"Queue: $sourceQueueKey was closed (pool shut down) while running the request. Try again later."
          Future.failed(HSServiceUnavailableException(msg))
      }
      .transform(
        identity,
        e => HSServiceUnavailableException(s"代理请求错误：${request.method.value} ${request.uri}。${e.toString}", cause = e))
  }

}
