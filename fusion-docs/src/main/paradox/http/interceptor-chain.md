# Interceptor Chain（拦截器链）

Interceptor Chain在应用开发中是一个很常见的设计模式，Fusion提供了`HttpInterceptor`接口来支持拦截器链模式。

Scala是一门面向对象和函数式编程相融合的一门语言，在函数式编程中可通过函数的嵌套和各种高级处理来实现拦截器链的功能。

## 应用拦截器链

@@snip [HttpHandlerDoc.scala](../../scala/docs/concept/HttpHandlerDoc.scala) { #applyHttpInterceptorChain }

在`applyHttpInterceptorChain`函数中，对拦截器链`filters`应用`foldLeft`函数，即可非常优雅的应用每一个拦截器。

## 拦截器示例

### 拦截器示例 1：trace

@@snip [TraceHttpFilter.scala](../../scala/docs/concept/HttpHandlerDoc.scala) { #TraceHttpInterceptor }

`TraceHttpInterceptor`实现了给接收到的HTTP请求添加 **trace id** 的功能，这在微服务中监控调用链非常有用。

### 拦截器示例 2：不做任务处理

@@snip [NothingHttpInterceptor.scala](../../scala/docs/concept/HttpHandlerDoc.scala) { #NothingHttpInterceptor }

直接返回`handler`，并不对HTTP请求过程做任务处理，就像 `NothingHttpIntercepter` 那样。你可以加入判断逻辑，

### 拦截器示例 3：终止调用链

某个拦截器想终止调用链的执行，在转换函数中抛出一个异常即可：

@@snip [NothingHttpInterceptor.scala](../../scala/docs/concept/HttpHandlerDoc.scala) { #TerminationHttpInterceptor }
