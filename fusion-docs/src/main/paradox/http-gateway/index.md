# HTTP网关代理

**fusion-http-gateway**

fusion-http-gateway基于 Akka HTTP 实现了一个强大的API网关代理功能，可通过 Lightbend Config 进行配置或编码实现。fusion-http-gateway支持对HTTP 1.0、1.1和2.0请求进行代理，同时还支持GRPC（使用HTTP 2实现）。

## 配置实现

fusion-http-gateway的配置主要分两部分（参考了Nginx）：

1. `upstreams`：设置上游服务
2. `locations`：指定需要代理的地址

#### upstream

```hocon
upstreams {
  # upstream服务名
  account {
    # 服务名，用于从服务发现机制中获取一个真实的访问地址
    serviceName = hongka-server-account
    # 使用Akka Discovery实现服务发现，默认使用 akka.discovery.method 指定的DiscoveryService
    discoveryMethod = nacos
    # 静态设置多个上游服务地址，当未指定serviceName时有效
    //targets = ["127.0.0.1:8888", "hostname.local"]
  }
}
```

#### location

```hocon
locations {
  # 要代理的地址前部（从URI PATH开头部分匹配）
  "/api/v4/platform" {
    # upstream服务名
    upstream = platform
    # 代理转发地址，未设置同 /api/v4/platform
    //proxy-to = "/api/v4/platform"
  }
}
```

## 编程实现

编程实现非常简单，使用常规的Akka HTTP功能即可。对Akka HTTP不熟悉的用户可阅读一本还不错的电子书：[《Scala Web 开发——基于Akka HTTP》](https://www.yangbajing.me/scala-web-development/)。

## 完整配置

详细配置见： @ref:[Fusion Http Gateway 配置](../configuration/http-gateway.md)。
