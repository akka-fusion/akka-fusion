/*
 * Copyright 2019 akka-fusion.com
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

package fusion.json.jackson.http

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ Materializer, QueueOfferResult }
import akka.{ actor => classic }
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ ArrayNode, ObjectNode }
import fusion.common.extension.{ FusionExtension, FusionExtensionId }
import fusion.core.http.HttpSourceQueue
import fusion.json.jackson.JacksonObjectMapperExtension
import helloscala.common.IntStatus
import helloscala.common.exception.{ HSException, HSHttpStatusException }

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.reflect.runtime.universe._

class JacksonHttpHelper private (override val classicSystem: ExtendedActorSystem) extends FusionExtension {
  val jacksonSupport: JacksonSupport = JacksonObjectMapperExtension(classicSystem).jacksonSupport
  import jacksonSupport._

  def buildRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`): HttpRequest = {
    val entity = data match {
      case null                    => HttpEntity.Empty
      case entity: UniversalEntity => entity
      case _                       => HttpEntity(ContentTypes.`application/json`, objectMapper.writeValueAsString(data))
    }
    HttpRequest(method, uri.withQuery(Uri.Query(uri.query() ++ params: _*)), headers, entity, protocol)
  }

  /**
   * 发送 Http 请求
   *
   * @param method   请求方法类型
   * @param uri      请求地址
   * @param params   请求URL查询参数
   * @param data     请求数据（将备序列化成JSON）
   * @param headers  请求头
   * @param protocol HTTP协议版本
   * @return HttpResponse
   */
  def singleRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`)(implicit system: classic.ActorSystem): Future[HttpResponse] = {
    val request = buildRequest(method, uri, params, data, headers, protocol)
    singleRequest(request)
  }

  /**
   * 发送 Http 请求
   *
   * @param request HttpRequest
   * @return
   */
  def singleRequest(request: HttpRequest)(implicit system: classic.ActorSystem): Future[HttpResponse] =
    Http().singleRequest(request)

  def hostRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil)(
      implicit httpSourceQueue: HttpSourceQueue,
      ec: ExecutionContext): Future[HttpResponse] = {
    val entity = if (data != null) {
      data match {
        case entity: RequestEntity => entity
        case _ =>
          HttpEntity(ContentTypes.`application/json`, objectMapper.writeValueAsString(data))
      }
    } else {
      HttpEntity.Empty
    }
    hostRequest(HttpRequest(method, uri.withQuery(Uri.Query(uri.query() ++ params: _*)), headers, entity))
  }

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。见：[[cachedHostConnectionPool()]]
   *
   * @param request         HttpRequest
   * @param httpSourceQueue 使用了CachedHostConnectionPool的 HTTP 队列
   * @return Future[HttpResponse]
   */
  def hostRequest(
      request: HttpRequest)(implicit httpSourceQueue: HttpSourceQueue, ec: ExecutionContext): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    httpSourceQueue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped =>
        Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed =>
        Future.failed(
          new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def makeRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, Any)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil): HttpRequest = {
    val entity = if (data != null) {
      data match {
        case entity: MessageEntity => entity
        case _                     => HttpEntity(ContentTypes.`application/json`, objectMapper.writeValueAsString(data))
      }
    } else {
      HttpEntity.Empty
    }
    val httpParams = params.map { case (key, value) => key -> value.toString }
    HttpRequest(method, uri.withQuery(Uri.Query(httpParams: _*)), headers, entity)
  }

  def httpEntity(v: Any): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, objectMapper.writeValueAsString(v))

  def mapObjectNode(response: HttpResponse)(implicit mat: Materializer): Future[ObjectNode] = {
    if (response.status.isSuccess()) {
      mapObjectNode(response.entity)
    } else {
      mapHttpResponseError(response)
    }
  }

  def mapObjectNode(entity: ResponseEntity)(implicit mat: Materializer): Future[ObjectNode] = {
    Unmarshal(entity).to[ObjectNode]
  }

  def mapArrayNode(response: HttpResponse)(implicit mat: Materializer): Future[ArrayNode] = {
    if (response.status.isSuccess()) {
      mapArrayNode(response.entity)
    } else {
      mapHttpResponseError(response)
    }
  }

  def mapArrayNode(entity: ResponseEntity)(implicit mat: Materializer): Future[ArrayNode] = {
    Unmarshal(entity).to[ArrayNode]
  }

  def mapHttpResponse[R](response: HttpResponse)(implicit ct: TypeTag[R], mat: Materializer): Future[R] = {
    if (response.status.isSuccess()) {
      Unmarshal(response.entity).to[R]
    } else {
      mapHttpResponseError(response)
    }
  }

  def mapHttpResponseEither[R](
      response: HttpResponse)(implicit ct: TypeTag[R], mat: Materializer): Future[Either[HSException, R]] = {
    implicit val ec: ExecutionContext = mat.executionContext
    mapHttpResponse(response).map(Right(_)).recoverWith {
      case e: HSException => Future.successful(Left(e))
    }
  }

  def mapHttpResponseError[R](response: HttpResponse)(implicit mat: Materializer): Future[R] = {
    implicit val ec: ExecutionContext = mat.executionContext
    if (response.entity.contentType.mediaType == MediaTypes.`application/json`) {
      Unmarshal(response.entity).to[JsonNode].flatMap { json =>
        val errCode = Option(json.get("status"))
          .orElse(Option(json.get("errCode")))
          .map(_.asInt(IntStatus.INTERNAL_ERROR))
          .getOrElse(IntStatus.INTERNAL_ERROR)
        val message =
          Option(json.get("message")).orElse(Option(json.get("msg"))).map(_.asText("")).getOrElse("HTTP响应错误")
        Future.failed(HSHttpStatusException(message, json, errCode, httpStatus = response.status.intValue()))
      }
    } else {
      Unmarshal(response.entity)
        .to[String]
        .flatMap(errMsg => Future.failed(HSHttpStatusException(errMsg, httpStatus = response.status.intValue())))
    }
  }

  def mapHttpResponseErrorEither[R](response: HttpResponse)(
      implicit mat: Materializer): Future[Either[HSException, R]] = {
    implicit val ec: ExecutionContext = mat.executionContext
    mapHttpResponseError(response).recoverWith {
      case e: HSException => Future.successful(Left(e))
    }
  }
}

object JacksonHttpHelper extends FusionExtensionId[JacksonHttpHelper] {
  override def createExtension(system: ExtendedActorSystem): JacksonHttpHelper = new JacksonHttpHelper(system)
}
