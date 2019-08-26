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

//package fusion.http.interceptor
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.model.HttpRequest
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.headers.RawHeader
//import akka.testkit.TestKit
//import fusion.http.HttpHandler
//import fusion.test.FusionTestFunSuite
//
//import scala.concurrent.Future
//
//class HttpInterceptorTest extends TestKit(ActorSystem()) with FusionTestFunSuite {
//  import system.dispatcher
//
//  test("testInterceptor") {
//    val interceptors = List(
//      new HttpInterceptor {
//        override def interceptor(inner: HttpHandler): HttpHandler = { req =>
//          println("first request")
//          inner(req.copy(headers = RawHeader("1", "1") +: req.headers)).map { resp =>
//            val response = resp.copy(headers = RawHeader("a", "a") +: resp.headers)
//            println(s"first response: $response")
//            response
//          }
//        }
//      },
//      new HttpInterceptor {
//        override def interceptor(inner: HttpHandler): HttpHandler = { req =>
//          println("second request")
//          inner(req.copy(headers = RawHeader("2", "2") +: req.headers)).map { resp =>
//            val response = resp.copy(headers = RawHeader("b", "b") +: resp.headers)
//            println(s"second response: $response")
//            response
//          }
//        }
//      },
//      new HttpInterceptor {
//        override def interceptor(inner: HttpHandler): HttpHandler = { req =>
//          println("three request")
//          inner(req.copy(headers = RawHeader("3", "3") +: req.headers)).map { resp =>
//            val response = resp.copy(headers = RawHeader("c", "c") +: resp.headers)
//            println(s"three response: $response")
//            response
//          }
//        }
//      })
//
//    val handler: HttpHandler = req =>
//      Future {
//        HttpResponse(headers = req.headers)
//      }
//
//    val httpHandler = interceptors.reverse.foldLeft(handler)((h, httpInterceptor) => httpInterceptor.interceptor(h))
//    val result      = httpHandler(HttpRequest()).futureValue
//    println(result)
//  }
//
//}
