# Http异步处理器

```scala
type HttpHandler = HttpRequest => Future[HttpResponse]
```

Fusion基于Akka HTTP实现HTTP 1.1、2.0协议的请求响应处理，同时支持GRPC。`HttpHandler`是`HttpRequest => Future[HttpResponse]`的类型定义，从接收到一个请求开始，返回一个异步的HTTP响应。

