# HTTP网关代理

**fusion-http-gateway**

fusion-http-gateway基于 Akka HTTP 实现了一个强大的API网关代理功能，可通过 Lightbend Config 进行配置或编码实现。fusion-http-gateway支持对HTTP 1.0、1.1和2.0请求进行代理，同时还支持GRPC（使用HTTP 2实现）。

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
