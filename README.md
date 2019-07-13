# Akka Fusion

Akka Fusion可以轻松创建独立的，生产级的基于Akka的应用程序。我们集成了Akka生态系统里常用及流行的组件，
因此您可以快速搭建开始你的应用。大多数Akka Fusion应用程序只需要很少的 [配置](https://lightbend.github.io/config/)。

更多内容请阅读文档：[https://ihongka.github.io/akka-fusion/](https://ihongka.github.io/akka-fusion/) 。

## 快速开始

```scala
object SampleApplication {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val ec     = system.dispatcher
    val sampleService   = new SampleService()
    val routes          = new SampleRoute(sampleService)
    FusionHttp(system).startAwait(routes.route)
  }
}

// Server
class SampleServer(val routes: SampleRoute) extends FusionServer

// Controller
class SampleRoute(sampleService: SampleService) extends AbstractRoute {
  override def route: Route = pathGet("hello") {
    parameters(('hello, 'year.as[Int].?(2019))).as(SampleReq) { req =>
      futureComplete(sampleService.hello(req))
    }
  }
}

// Request、Response Model
case class SampleReq(hello: String, year: Int)
case class SampleResp(hello: String, year: Int, language: String)

// Service
class SampleService()(implicit ec: ExecutionContext) {

  def hello(req: SampleReq): Future[SampleResp] = Future {
    SampleResp(req.hello, req.year, "scala")
  }
}
```

测试：
```
$ curl -i http://localhost:8000/hello?hello=Hello
HTTP/1.1 200 OK
Fusion-Server: fusion/0:0:0:0:0:0:0:0:8000
Server: akka-http/10.1.7
Date: Sat, 16 Feb 2019 11:24:37 GMT
Content-Type: application/json
Content-Length: 48

{"hello":"Hello","year":2019,"language":"scala"}
```
