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

package fusion.http.server

import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import fusion.core.model.ApiResult
import fusion.http.util.BaseHttpUtils
import fusion.json.jackson.http.JacksonSupport
import helloscala.common.exception.HSException

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.reflect.runtime.universe._

trait JacksonDirectives extends BaseHttpUtils {
  val jacksonSupport: JacksonSupport

  def jacksonAs[T](implicit ev: TypeTag[T]): FromRequestUnmarshaller[T] = {
    import jacksonSupport._
    as[T]
  }

  def completionStageComplete(
      future: java.util.concurrent.CompletionStage[AnyRef],
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK
  ): Route = {
    import scala.compat.java8.FutureConverters._
    val f: AnyRef => Route = objectComplete(_, needContainer, successCode)
    onSuccess(future.toScala).apply(f)
  }

  def futureComplete(
      future: Future[AnyRef],
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK
  ): Route = {
    val f: AnyRef => Route = objectComplete(_, needContainer, successCode)
    onSuccess(future).apply(f)
  }

  @tailrec
  final def objectComplete(
      obj: Any,
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK
  ): Route = {
    obj match {
      case Right(result) =>
        objectComplete(result, needContainer, successCode)

      case Left(e: HSException) =>
        objectComplete(e, needContainer, successCode)

      case Some(result) =>
        objectComplete(result, needContainer, successCode)

      case None =>
        complete(jsonEntity(StatusCodes.NotFound, "数据不存在"))

      case response: HttpResponse =>
        complete(response)

      case responseEntity: ResponseEntity =>
        complete(HttpResponse(successCode, entity = responseEntity))

      case status: StatusCode =>
        complete(status)

      case result =>
        import jacksonSupport._
        val resp = if (needContainer) ApiResult.success(result) else result
        complete((successCode, resp))
    }
  }

  def eitherComplete[T](either: Either[HSException, T]): Route = {
    either match {
      case Right(result) =>
        objectComplete(result)
      case Left(e) =>
        objectComplete(e)
    }
  }
}
