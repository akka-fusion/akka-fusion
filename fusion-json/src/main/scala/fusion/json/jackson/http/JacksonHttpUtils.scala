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

package fusion.json.jackson.http

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import fusion.json.jackson.Jackson
import fusion.json.jackson.http.JacksonSupport._
import helloscala.common.IntStatus
import helloscala.common.exception.HSException
import helloscala.common.exception.HSHttpStatusException

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.ClassTag

object JacksonHttpUtils {
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

  def mapHttpResponse[R: ClassTag](response: HttpResponse)(implicit mat: Materializer): Future[R] = {
    implicit val ec: ExecutionContext = mat.executionContext
    if (response.status.isSuccess()) {
      Unmarshal(response.entity).to[R]
    } else {
      mapHttpResponseError(response)
    }
  }

  def mapHttpResponseEither[R: ClassTag](response: HttpResponse)(
      implicit mat: Materializer): Future[Either[HSException, R]] = {
    implicit val ec: ExecutionContext = mat.executionContext
    mapHttpResponse(response).map(Right(_)).recoverWith {
      case e: HSException => Future.successful(Left(e))
    }
  }

  def mapHttpResponseList[R](response: HttpResponse)(implicit ev1: ClassTag[R], mat: Materializer): Future[List[R]] = {
    implicit val ec: ExecutionContext = mat.executionContext
    if (response.status.isSuccess()) {
      Unmarshal(response.entity)
        .to[ArrayNode]
        .map { array =>
          array.asScala
            .map(node => Jackson.defaultObjectMapper.treeToValue(node, ev1.runtimeClass).asInstanceOf[R])
            .toList
        }(if (ec eq null) mat.executionContext else ec)
    } else {
      mapHttpResponseError[List[R]](response)
    }
  }

  def mapHttpResponseListEither[R](
      response: HttpResponse)(implicit ev1: ClassTag[R], mat: Materializer): Future[Either[HSException, List[R]]] = {
    implicit val ec: ExecutionContext = mat.executionContext
    mapHttpResponseList(response).map(Right(_)).recoverWith {
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
