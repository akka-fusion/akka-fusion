# Akka Extension（扩展）

Akka Extension是Akka提供的一套可插拔的、用来增强Akka能力的机制，`akka-cluster`等很多内建功能也是基于它实现的。同时，Akka Extension还提供了某种程度上的依赖管理功能，Fusion也基于它实现了**akka-fusion**框架的模块化管理。

Akka Extension提供了两个基本组件：`Extension`和`ExtensionId`。每个Akka扩展在同一个`ActorSystem`内保证只加载一次，你可以选择按需加载，也可以选择随`ActorSystem`创建时即加载。有关这部分的内容参考接下来的 [从配置加载](#从配置加载) 。

这样，在你的应用中只需要全局保证一个ActorSystem即可，其它的服务、资源都可以通过 Akka Extension 来维护。同时，你可以很自然的在自己的Akka Extension实例内部引用其它的Akka Extension（需要保证它们都使用同一个ActorSystem）。
这可能是在不使用IOC（如Spring、Guice等）情况下最好的进行依赖管理机制之一。

## 从配置加载

```hocon
akka {
  # 用于为Akka制作的第三方扩展库，需要随ActorSystem一起加载。
  # 若最终用户需要在`application.conf`中配置扩展，应使用`extensions`属性配置。
  library-extensions = ${?akka.library-extensions} ["akka.serialization.SerializationExtension"]

  # 在此列出需要加载的多个自定义扩展，需要使用类的全限定名。
  extensions = []
}
```

## 构建扩展

akka-fusion在提供了 `FusionExtension` 帮助trait来构建Akka Extension。

@@snip [FusionExtension.scala](../../../../../fusion-core/src/main/scala/fusion/core/extension/FusionExtension.scala) { #FusionExtension }

`FusionExtension`在默认`Extension`基础之上添加了`ActorSystem`引用，并通过`FusionCore`提供了`Configuration`（对Lightbend Config的增强包装）。

## 通过Akka Extension来管理资源

接下来为`FusionJdbc`作为示例，说明`FusionExtension`是怎样来管理我们的数据库访问资源的。`FusionJdbc`管理了一个或多个数据库连接池，连接池通过 [HikariCP](https://github.com/brettwooldridge/HikariCP) 实现。

@@snip [FusionJdbc.scala](../../../../../fusion-jdbc/src/main/scala/fusion/jdbc/FusionJdbc.scala) { #FusionJdbc }

`FusionJdbc`将由Akka保证在同一个ActorSystem中只被实例化一次，就像Spring框架里的`@Service`注解、Guice框架的`Singleton`注解一样，它们都是 **单例** 。

@@snip [FusionJdbc.scala](../../../../../fusion-jdbc/src/main/scala/fusion/jdbc/FusionJdbc.scala) { #JdbcComponents }

`JdbcComponents`继承了`Components`，`Components`提供了一个保存同一类型组件的多个实例的优秀方案。它基于 Lightbend Config 实现了可配置化，通过构造函数传入的配置路径（id）来决定引用哪一个配置，并保存id的实例的对应关系。

请关注`FusionCore(system).shutdowns.beforeActorSystemTerminate`这行代码，它使用`CoordinatedShutdown`来协调资源的关闭，它将在`ActorSystem`终止前关闭所有数据库连接池。更多内容请参阅： @ref:[FusionCore#shutdowns](./fusion-core.md#shutdowns)

#### Components

`Components` 提供代码实现如下：

@@snip [Components.scala](../../../../../fusion-core/src/main/scala/fusion/core/util/Components.scala) { #Components }

## 通过Akka Extension来管理服务（依赖）

修定你有3个服务：

- FileService：统一的文件服务，如提供用户头像链接
- UserService：用户服务
- LoginService：实现用户登录、注册等业务逻辑

你可以如下定义3个服务

@@snip [CustomService.scala](../../../main/scala/docs/extension/customservice/CustomService.scala)

通过以上代码，你看到了怎样使用Akka Extension来实现服务的依赖管理。所有的服务之间只有一个显示依赖：`ActorSystem`。因为我们的框架是基于Akka的，所以我们认为显示依赖`ActorSystem`并不是一个问题。
