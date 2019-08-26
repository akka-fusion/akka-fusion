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

package fusion.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer

import scala.concurrent.Future

trait HttpClient extends AutoCloseable {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer = ActorMaterializer()
  def singleRequest(req: HttpRequest): Future[HttpResponse]

  def request(req: HttpRequest): Future[HttpResponse] = {
    val f = singleRequest(req)
    fallback match {
      case Some(fb) => Future.firstCompletedOf(List(f, fb()))(materializer.executionContext)
      case _        => f
    }
  }
  def fallback: Option[() => Future[HttpResponse]] = None
}
