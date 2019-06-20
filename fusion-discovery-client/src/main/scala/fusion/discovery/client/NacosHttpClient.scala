package fusion.discovery.client

import java.util.concurrent.ConcurrentHashMap

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Authority
import akka.stream.ActorMaterializer
import akka.stream.QueueOfferResult
import com.typesafe.scalalogging.StrictLogging
import fusion.http.HttpSourceQueue
import fusion.http.util.HttpUtils
import helloscala.common.exception.HSServiceUnavailableException
import helloscala.common.util.Utils

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.reflect.ClassTag

/**
 * TODO fallback
 */
final class NacosHttpClient private (
    val namingService: FusionNamingService,
    val materializer: ActorMaterializer,
    val httpSourceQueueBufferSize: Int)
    extends DiscoveryHttpClient
    with StrictLogging {

  private val httpSourceQueueMap = new ConcurrentHashMap[Authority, HttpSourceQueue]()
  implicit private def mat       = materializer

  override def hostRequestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T] = {
    import fusion.http.util.JacksonSupport._
    hostRequest(req).flatMap(response => HttpUtils.mapHttpResponse(response))(materializer.executionContext)
  }

  override def hostRequestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = {
    hostRequest(req).flatMap(response => HttpUtils.mapHttpResponseList(response))(materializer.executionContext)
  }

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。
   *
   * @param req 发送请求，将通过Nacos替换对应服务(serviceName)为实际的访问地址
   * @return Future[HttpResponse]
   */
  override def hostRequest(req: HttpRequest): Future[HttpResponse] = {
    val request         = buildHttpRequest(req)
    val uri             = request.uri
    val responsePromise = Promise[HttpResponse]()
    val queue = httpSourceQueueMap.computeIfAbsent(
      uri.authority,
      _ => HttpUtils.cachedHostConnectionPool(uri, httpSourceQueueBufferSize)(system, materializer))
    queue
      .offer(request -> responsePromise)
      .flatMap {
        case QueueOfferResult.Enqueued => responsePromise.future
        case QueueOfferResult.Dropped =>
          Future.failed(HSServiceUnavailableException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed =>
          httpSourceQueueMap.remove(uri.authority)
          val msg = "Queue was closed (pool shut down) while running the request. Try again later."
          Future.failed(HSServiceUnavailableException(msg))
      }(materializer.executionContext)
  }

  def singleRequestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T] = {
    import fusion.http.util.JacksonSupport._
    singleRequest(req).flatMap(response => HttpUtils.mapHttpResponse(response))(materializer.executionContext)
  }

  def singleRequestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = {
    singleRequest(req).flatMap(response => HttpUtils.mapHttpResponseList(response))(materializer.executionContext)
  }

  override def buildUri(uri: Uri): Uri = {
    val host = uri.authority.host
    if (host.isNamedHost()) {
      val serviceName = host.address()
      val inst        = Utils.requireNonNull(namingService.selectOneHealthyInstance(serviceName), s"服务： $serviceName 不存在")
      val newUri      = uri.withAuthority(inst.ip, inst.port)
      logger.debug(s"build uri: $uri to $newUri")
      newUri
    } else {
      uri
    }
  }

  override def close(): Unit = httpSourceQueueMap.clear()
}

object NacosHttpClient {

  def apply(namingService: FusionNamingService, materializer: ActorMaterializer): NacosHttpClient =
    apply(namingService, materializer, 512)

  def apply(
      namingService: FusionNamingService,
      materializer: ActorMaterializer,
      httpSourceQueueBufferSize: Int): NacosHttpClient =
    new NacosHttpClient(namingService, materializer, httpSourceQueueBufferSize)
}
