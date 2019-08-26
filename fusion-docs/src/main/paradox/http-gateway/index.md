# HTTP 代理网关

**fusion-http-gateway**

fusion-http-gateway基于 Akka HTTP 实现了一个强大的API网关代理功能，可通过 Lightbend Config 进行配置或编码实现。fusion-http-gateway支持对HTTP 1.0、1.1和2.0请求进行代理，同时还支持GRPC（使用HTTP 2实现）。

## 特性

- 支持HTTP 1.0、1.1、2.0
- 支持GRPC
- 支持服务发现（通过 Akka Discovery）
- 支持自定义代理策略（如：修改上传body大小限制、请求超时等）
- 支持HTTP拦截器
- 支持熔断（通过 Akka CircuitBreaker 对某一地址设置熔断策略）

最后，若你想要更大的可控性，可以继承 `HttpGatewayComponent` 实现你自己的HTTP代理组件。

## 配置实现

fusion-http-gateway的配置主要分两部分（参考了Nginx）：

1. `upstreams`：设置上游服务
2. `locations`：指定需要代理的地址

#### upstream

@@snip [template.conf#upstreams](../../../../../fusion-http-gateway/src/main/resources/template.conf) { #upstreams }

#### location

@@snip [template.conf#locations](../../../../../fusion-http-gateway/src/main/resources/template.conf) { #locations }

#### 完整配置

详细配置见： @ref:[Fusion Http Gateway 配置](../configuration/http-gateway.md)。

## 编程实现

编程实现非常简单，使用常规的Akka HTTP功能即可。对Akka HTTP不熟悉的用户可阅读一本还不错的电子书：[《Scala Web 开发——基于Akka HTTP》](https://www.yangbajing.me/scala-web-development/)。

## 自定义 `HttpGatewayComponent`

akka fusion提供了 `HttpGatewayComponent` 抽象类以使用户可以完全自定义HTTP代理。只需要继承 `HttpGatewayComponent` ，并将类的完整限定名配置到 `fusion.http.<default>.gateway.class` 属性即可。akka fusion使用用户提供的类来实例化HTTP代理组件。下面是一个示例：

**配置**
```hocon
fusion.http.default.gateway {
  class = fusion.docs.gateway.CustomHttpGatewayComponent
}
```

**自定义HTTP网关代理组件**
```scala
package fusion.docs.gateway

final class CustomHttpGatewayComponent(id: String, system: ActorSystem) 
    extends HttpGatewayComponent 
    with SessionDirectives {
  private val aggregate = BackendAggregate(system)

  override def route: Route = {
    validationSession(aggregate.sessionComponent) {                        // 校验session
      logging(aggregate.sessionComponent, 
          logMsg => aggregate.kafkaMessageService.processLogMsg(logMsg)) { // 记录日志
        super.route  
      }
    }
  }
}
```

这里我们使用 `validationSession` Directive来校验请求是否包含了有效的用户登录会话信息，`logging`指定记录HttpRequest、HttpResponse日志；可以看到，这里将请求、响应日志发送到了 Kafka 中，再由其它服务从 Kafka 里消费日志以对其进行进一步处理。
