# Http Route（异步处理器）

```scala
type Route = RequestContext => Future[RouteResult]
```

Fusion基于Akka HTTP实现了HTTP 1.0、1.1和2.0协议的请求响应处理，同时还支持GRPC。`Route`是`RequestContext => Future[RouteResult]`的类型定义，从接收到一个请求开始，返回一个异步的HTTP响应。原始的`HttpRequest`被包裹在一个`RequestContext`请求上下文中，而响应结果也使用了`RouteResult`进行封装。

## RequestContext

`RequestContext`的一个典型实现如下：

```scala
trait RequestContext {
  val request: HttpRequest
  val unmatchedPath: Uri.Path
  implicit def executionContext: ExecutionContextExecutor
  implicit def materializer: Materializer
  def log: LoggingAdapter
  def settings: RoutingSettings
  def parserSettings: ParserSettings
  def reconfigure(
    executionContext: ExecutionContextExecutor = executionContext,
    materializer:     Materializer             = materializer,
    log:              LoggingAdapter           = log,
    settings:         RoutingSettings          = settings): RequestContext
  def complete(obj: ToResponseMarshallable): Future[RouteResult]
  def reject(rejections: Rejection*): Future[RouteResult]
  def redirect(uri: Uri, redirectionType: Redirection): Future[RouteResult]
  def fail(error: Throwable): Future[RouteResult]
  // ....
}
```

**`request`**

保存了原始的`HttpRequest`对象。

**`unmatchedPath`**

保存了当前Akka HTTP Routing层级下还未匹配的路径。如下面的路由定义，在`account`这一级，请求`Uri.Path`: `/api/v4/account/user/page`对应的`unmatchedPath`保持的未匹配路径为：`/user/page`。 

**`complete`**

请求正常完成时，返回数据通过`complete`函数来响应。`complete`函数通过`ToResponseMarshallable`来决定数据类型该怎么处理（序列化），Akka HTTP默认已提供了大部分数据类型的处理方式，我们可以很方便的对自定义数据实现`ToResponseMarshallable`。对应JSON类型的数据响应，[https://github.com/hseeberger/akka-http-json](https://github.com/hseeberger/akka-http-json) 是一个很好的起点。

**`reject`**

`reject`顾名思义，调用它将拒绝当前请求。它需要传一个或多个实现了`Rejection`接口的类型。akka-fusion默认的reject处理如下：

@@snip [BaseRejectionBuilder](../../../../../fusion-http-client/src/main/scala/fusion/http/util/BaseRejectionBuilder.scala) { #rejectionBuilder }

**`redirect`**

`redirect`在你需要向请求方返回重定向响应时使用。它有两个参数：

1. `uri: Uri`：需要生定向的地址
2. `redirectionType: Redirection`：HTTP响应状态码，现在可选择的状态码有300到308中的一个

**`fail`**

返回异常的快捷函数，大多数情况下不需要使用到这个。

## RouteResult

`akka.http.scaladsl.server.RouteResult`接口有两个子类（定义类似）：

```scala
final case class Complete(response: HttpResponse) extends RouteResult
final case class Rejected(rejections: Seq[Rejection]) extends RouteResult
```

`Complete`很直观了，代表请求已完成（HTTP请求已完成，但业务有可能错误）。

而`Rejected`代表请求被拒绝，一般用于HTTP方法不对、URL路径未找到、请求实体过大等情况。
