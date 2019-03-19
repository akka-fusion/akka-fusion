# 连接 Nacos

## 依赖

要使用Fusion提供到服务发现、注册客户端功能，需添加以下依赖：

@@dependency[sbt,Maven,Gradle] {
  group="com.helloscala.fusion"
  artifact="fusion-discovery-client_$scala.binary_version$"
  version="$version$"
}

## 开始使用

在`application.conf`配置文件中添加以下配置指定Nacos服务端：

```hocon
fusion.discovery {
  enable = true
  nacos {
    serverAddr = "10.0.5.36:8849"
    namespace = "7bf36554-e291-4789-b5fb-9e515ca58ba0"
    dataId = "hongka.file.app"
    group = "DEFAULT_GROUP"
    timeoutMs = 3000
    serviceName = "hongka-file-app"
  }
}
```

各配置荐含义为：

- `enable`：启用Fusion Discovery功能，默认值为`false`
- `nacos.serverAddr`：Nacos服务地址
- `nacos.namespace`：Nacos服务命名空间（可选）
- `nacos.dataId`：配置ID
- `nacos.group`：配置分组
- `nacos.timeoutMs`：获取注册配置时的超时时间（单位：毫秒），默认值为`3000`
- `nacos.serviceName`：注册到Nacos时的服务端，不指定则使用`fusion.name`配置

### 获取配置

添加以上配置以后，通常我们可以使用`Configuration.fromDiscovery()`来自动从Nacos服务获取配置信息。当未启用fusion-discovery或Nacos连接失败时将使用本地配置。

### 注册服务到Nacos

Fusion提供了Akka扩展：`FusionNacos`自动实现注册服务到Nacos。使用`ActorSystem`的实例调用`FusionNacos`即可，`FusionNacos(system)`。代码示例如下：

```scala
val configuration = Configuration.fromDiscovery()
val system = ActorSystem("name", configuration.underlying)
FusionNacos(system)
```

## 更多示例

@@snip [NacosServiceFactoryTest](../../../../../fusion-discovery-client/src/test/scala/fusion/discovery/client/nacos/NacosServiceFactoryTest.scala) { #NacosServiceFactoryTest }

代码见： @github[NacosServiceFactoryTest.scala](../../../../../fusion-discovery-client/src/test/scala/fusion/discovery/client/nacos/NacosServiceFactoryTest.scala) 。
