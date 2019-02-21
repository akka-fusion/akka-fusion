# Akka Fusion

Akka Fusion可以轻松创建独立的，生产级的基于Akka的应用程序。我们集成了Akka生态系统里常用及流行的组件，可以快速搭建你的应用。

大多数Akka Fusion应用程序只需要很少的 @ref[配置](configuration/index.md)。

官网：[https://ihongka.github.io/akka-fusion/](https://ihongka.github.io/akka-fusion/)。

Akka Fusion以Akka工具库为基础，为用户提供开箱及用的微服务、云应用框架。Akka Fusion集成了Scala/Akka社区各种优秀的开源组件，
让你可以快速开始你的微服务开发，就像Spring Boot、Spring Cloud一样。但Akka Fusion更加强大、易用、安全，
它具有完备的编译期检查，让你在开发阶段即可排除更多的错误。

## 快速开始

代码：

@@snip [SampleApplication](../scala/fusion/docs/sample/SampleApplication.scala) { #SampleApplication }

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

## 目录

@@toc { depth=2 }

@@@ index

* [intro](intro/index.md)
* [jdbc](jdbc/index.md)
* [data-mongodb](data-mongodb/index.md)
* [data-kafka](data-kafka/index.md)
* [configuration](configuration/index.md)

@@@
