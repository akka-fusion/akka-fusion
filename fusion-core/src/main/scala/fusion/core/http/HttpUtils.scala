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

package fusion.core.http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{ Marshal, Marshaller }
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{ Unmarshal, Unmarshaller }
import akka.stream.Materializer

import scala.collection.immutable
import scala.concurrent.Future

final class ResponseAs(val response: Future[HttpResponse])(implicit mat: Materializer) {
  import mat.executionContext
  def responseAs[R](implicit um: Unmarshaller[ResponseEntity, R]): Future[R] =
    response.flatMap(resp => Unmarshal(resp.entity).to[R])

  def onSuccessResponseAs[R](implicit um: Unmarshaller[ResponseEntity, R]): Future[R] =
    response.flatMap {
      case resp if resp.status.isSuccess() => Unmarshal(resp.entity).to[R]
      case resp                            => Future.failed(new IllegalStateException(s"Http response is not success, response is $resp."))
    }
}

final class HttpUtils private ()(implicit system: ActorSystem[_]) {
  def singleRequest[A](
      method: HttpMethod = HttpMethods.GET,
      uri: Uri = "/",
      headers: immutable.Seq[HttpHeader] = Nil,
      entity: A = null,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`)(
      implicit
      m: Marshaller[A, RequestEntity]): ResponseAs = {
    import system.executionContext
    val entityF = if (null == entity) Future.successful(HttpEntity.Empty) else Marshal(entity).to[RequestEntity]
    val responseF = entityF.flatMap { entity =>
      val request = HttpRequest(method, uri, headers, entity, protocol)
      Http(system).singleRequest(request)
    }
    new ResponseAs(responseF)
  }
}

object HttpUtils {
  def apply(system: ActorSystem[_]): HttpUtils = new HttpUtils()(system)
}
