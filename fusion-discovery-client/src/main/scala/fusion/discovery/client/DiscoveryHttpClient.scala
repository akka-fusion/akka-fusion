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

package fusion.discovery.client

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.pattern.CircuitBreaker
import akka.stream.QueueOfferResult
import com.typesafe.config.ConfigFactory
import fusion.core.setting.CircuitBreakerSetting
import fusion.http.HttpSourceQueue
import fusion.http.client.HttpClient
import fusion.http.util.HttpUtils
import helloscala.common.Configuration
import helloscala.common.exception.HSBadGatewayException
import helloscala.common.exception.HSServiceUnavailableException

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

/**
 * {
 *   # HttpSourceQueue队列大小
 *   queue-buffer-size = 512
 *   # 是否启用熔断器
 *   circuit.enable = true
 *   # 最大连续失败次数
 *   circuit.max-failures = 5
 *   # 单次服务调用超时
 *   circuit.call-timeout = 10.seconds
 *   # 熔断器开断后，再次尝试接通断路器的时间
 *   circuit.reset-timeout = 60.seconds
 * }
 */
final class DiscoveryHttpClientSetting(val c: Configuration) {
  def queueBufferSize: Int = c.getOrElse("queue-buffer-size", 512)
  def discoveryMethod: Option[String] = c.get[Option[String]]("discovery-method")
  def discoveryTimeout: FiniteDuration = c.getOrElse("discovery-timeout", 10.seconds)
  val circuit = CircuitBreakerSetting(c, "circuit")
}

trait DiscoveryHttpClient extends HttpClient {
  implicit val system: ActorSystem
  implicit protected def ec: ExecutionContext = materializer.executionContext
  val clientSetting: DiscoveryHttpClientSetting
  private val httpSourceQueueMap = new ConcurrentHashMap[(String, Int), HttpSourceQueue]()
  private val circuitBreaker = {
    if (clientSetting.circuit.enable) {
      Some(
        CircuitBreaker(
          system.scheduler,
          clientSetting.circuit.maxFailures,
          clientSetting.circuit.callTimeout,
          clientSetting.circuit.resetTimeout))
    } else {
      None
    }
  }
  def buildUri(uri: Uri): Future[Uri]

  def buildHttpRequest(req: HttpRequest): Future[HttpRequest] = {
    buildUri(req.uri).map(newUri => req.withUri(newUri))
  }

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。
   *
   * @param req 发送请求，将通过Nacos替换对应服务(serviceName)为实际的访问地址
   * @return Future[HttpResponse]
   */
  def hostRequest(req: HttpRequest): Future[HttpResponse] = {
    buildHttpRequest(req).flatMap { request =>
      val uri = request.uri
      val sourceQueueKey = Tuple2(uri.authority.host.address(), uri.effectivePort)
      val responsePromise = Promise[HttpResponse]()
      val queue = generateQueue(uri, sourceQueueKey)
      val noAuthorityRequest = request.copy(uri = HttpUtils.clearAuthority(uri))
      val responseF = queue
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

      circuitBreaker.map(_.withCircuitBreaker(responseF)).getOrElse(responseF)
    }
  }

  @inline private def generateQueue(uri: Uri, sourceQueueKey: (String, Int)): HttpSourceQueue = {
    httpSourceQueueMap.computeIfAbsent(
      sourceQueueKey,
      _ => HttpUtils.cachedHostConnectionPool(uri, clientSetting.queueBufferSize))
  }

  override def request(req: HttpRequest): Future[HttpResponse] = {
    val f = hostRequest(req)
    fallback match {
      case Some(fb) => Future.firstCompletedOf(List(f, fb()))(materializer.executionContext)
      case _        => f
    }
  }

  override def singleRequest(req: HttpRequest): Future[HttpResponse] = {
    buildHttpRequest(req).flatMap(request => Http().singleRequest(request))
  }

  override def close(): Unit = {
    httpSourceQueueMap.forEach((_, queue) => queue.complete())
    httpSourceQueueMap.clear()
  }

}

object DiscoveryHttpClient {

  def apply(system: ActorSystem, clientSetting: DiscoveryHttpClientSetting): DiscoveryHttpClient =
    new AkkaDiscoveryHttpClient(clientSetting)(system)

  def apply(system: ActorSystem): DiscoveryHttpClient =
    apply(system, new DiscoveryHttpClientSetting(Configuration(ConfigFactory.parseString("{}"))))
}
