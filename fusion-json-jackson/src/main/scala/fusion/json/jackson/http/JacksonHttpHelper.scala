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
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ ArrayNode, ObjectNode }
import fusion.common.extension.{ FusionExtension, FusionExtensionId }
import fusion.json.jackson.{ Jackson, JacksonObjectMapperExtension }
import helloscala.common.IntStatus
import helloscala.common.exception.{ HSException, HSHttpStatusException }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.runtime.universe._

class JacksonHttpHelper private (override val classicSystem: ExtendedActorSystem) extends FusionExtension {
  val jacksonSupport: JacksonSupport = JacksonObjectMapperExtension(classicSystem).jacksonSupport
  import jacksonSupport._

  def httpEntity(v: Any): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, Jackson.stringify(v))

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
