package fusion.discovery.http

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Authority
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import fusion.core.http.HttpSourceQueue
import fusion.discovery.client.FusionNamingService
import helloscala.common.exception.HSBadGatewayException
import helloscala.common.exception.HSServiceUnavailableException
import helloscala.common.util.Utils

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

final class HttpClient private (
    val namingService: FusionNamingService,
    val materializer: ActorMaterializer,
    val httpSourceQueueBufferSize: Int = 512)
    extends AutoCloseable {

  private val httpSourceQueueMap              = new ConcurrentHashMap[Authority, HttpSourceQueue]()
  implicit private def system: ActorSystem    = materializer.system
  implicit private def mat: ActorMaterializer = materializer

  /**
   * 发送HTTP请求
   *
   * @param req HTTP请求 [[HttpRequest]]
   * @return HTTP响应 [[HttpResponse]]
   */
  def request(req: HttpRequest): Future[HttpResponse] = hostRequest(req)

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。
   *
   * @param req 发送请求，将通过Nacos替换对应服务(serviceName)为实际的访问地址
   * @return Future[HttpResponse]
   */
  def hostRequest(req: HttpRequest): Future[HttpResponse] = {
    val request         = buildHttpRequest(req)
    val uri             = request.uri
    val responsePromise = Promise[HttpResponse]()
    httpSourceQueueMap
      .computeIfAbsent(uri.authority, _ => cachedHostConnectionPool(uri))
      .offer(request -> responsePromise)
      .flatMap {
        case QueueOfferResult.Enqueued    => responsePromise.future
        case QueueOfferResult.Dropped     => Future.failed(HSBadGatewayException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed =>
          httpSourceQueueMap.remove(uri.authority)
          val msg = "Queue was closed (pool shut down) while running the request. Try again later."
          Future.failed(HSServiceUnavailableException(msg))
      }(mat.executionContext)
  }

  def singleRequest(req: HttpRequest): Future[HttpResponse] = Http().singleRequest(buildHttpRequest(req))

  def buildHttpRequest(req: HttpRequest): HttpRequest = req.withUri(buildUri(req.uri))

  def buildUri(uri: Uri): Uri = {
    val serviceName = uri.authority.host.address()
    val inst        = Utils.requireNonNull(namingService.selectOneHealthyInstance(serviceName), s"服务： $serviceName 不存在")
    uri.withAuthority(inst.ip, inst.port)
  }

  private def cachedHostConnectionPool(uri: Uri)(implicit system: ActorSystem, mat: Materializer): HttpSourceQueue = {
    val ua = uri.authority
    val poolClientFlow = uri.scheme match {
      case "http"  => Http().cachedHostConnectionPool[Promise[HttpResponse]](ua.host.address(), ua.port)
      case "https" => Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]](ua.host.address(), ua.port)
      case _       => throw new IllegalArgumentException(s"URI: $uri 不是有效的 http 或 https 协议")
    }
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](httpSourceQueueBufferSize, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) => p.success(resp)
        case (Failure(e), p)    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  override def close(): Unit = httpSourceQueueMap.clear()
}

object HttpClient {

  def apply(namingService: FusionNamingService, materializer: ActorMaterializer): HttpClient =
    new HttpClient(namingService, materializer)
}
