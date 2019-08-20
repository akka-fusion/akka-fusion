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
