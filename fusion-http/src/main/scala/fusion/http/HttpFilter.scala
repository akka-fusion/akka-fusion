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

//package fusion.http
//
//import akka.actor.ExtendedActorSystem
//import akka.http.scaladsl.model.HttpRequest
//import akka.http.scaladsl.model.HttpResponse
//
//import scala.concurrent.ExecutionContextExecutor
//import scala.concurrent.Future
//
//trait HttpFilter {
//  def execute(handler: HttpHandler, system: ExtendedActorSystem): HttpHandler
//
//  def filter(request: HttpRequest): (HttpRequest, HttpResponse => Future[HttpResponse]) = {
//    (request, response => Future.successful(response))
//  }
//
//  def filterRequest(request: HttpRequest): HttpRequest = request
//
//  def filterResponse(response: HttpResponse): Future[HttpResponse] = {
//    Future.successful(response)
//  }
//}
//
//abstract class AbstractHttpFilter(val system: ExtendedActorSystem) extends HttpFilter {
//  implicit protected def ec: ExecutionContextExecutor = system.dispatcher
//
//  final override def execute(handler: HttpHandler, system: ExtendedActorSystem): HttpHandler = {
//    this.execute(handler)
//  }
//
//  protected def execute(handler: HttpHandler): HttpHandler
//}
