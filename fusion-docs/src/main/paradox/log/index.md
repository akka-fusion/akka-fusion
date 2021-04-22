# 日志

使用 `fusion-log` 库，需要添加以下依赖：

@@dependency[sbt,Maven,Gradle] {
  group="com.helloscala.fusion"
  artifact="fusion-log_$scala.binary_version$"
  version="$version$"
}

应用开发、运行时，日志作为调试、留痕的重要工具非常重要，Akka Fusion 对此提供了开箱即用的支持。

1. 预置的日志 encoder 配置
2. 通过 `filebeat` 输出日志到 Elasticsearch
3. 良好的自定义支持

## 输出日志到终端

`logback.xml` 日志文件内容：

@@snip [logback-stdout.xml](../../../../../fusion-log/example/logback-stdout.xml)

## 输出日志到文件

`logback.xml` 日志文件内容：

@@snip [logback-file.xml](../../../../../fusion-log/example/logback-file.xml)

| 属性 | 说明 |
|-----|-----|
| `fusion.log.logback.dir` | 指定输出日志文件名 |
| `fusion.log.logback.file` | 指定输出日志保存目录 |

默认提供的 `FILE` logback 日志 **Appender** 为了方便 filebeat 读取并存储日志到 Elasticsearch，使用JSON格式输出日志。[file-appender.xml](#file-appender-xml) 可以查阅详细的配置及说明。

### 异步输出到文件

添加一个 `AsyncAppender` 来包装 `RollingFileAppender`，即可以异步的方式写文件写入日志。

```xml
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>1024</queueSize>
        <neverBlock>true</neverBlock>
        <appender-ref ref="FILE" />
    </appender>

    <root level="INFO">
        <appender-ref ref="ASYNC_FILE"/>
    </root>
```

## 在 Scala/Java 应用中使用

应用日志使用 logback 输出，可由程序启动命令参数指定日志配置文件路径。如：`-Dlogback.configurationFile=${__APP_PATH__}/logback.xml`。

### scala-logging

在 Scala 应用中，可以使用 `StrictLogging` 或 `LazyLogging` 来引入 `Logger` 变量，这个特性由 [scala-logging](https://github.com/lightbend/scala-logging) 库提供。

```scala
class MyClass extends StrictLogging {
  logger.debug(s"Some $expensive message!")
  
  logger.whenDebugEnabled {
    println("This would only execute when the debug level is enabled.")
    (1 to 10).foreach(x => println("Scala logging is great!"))
  }
}
```

可以看到，在代码里简单的 extends/with `StrictLogging` 这个 trait，就可以直接使用 `logger` 变量来调用它上面的各种日志方法。另外，也不在需要在日志消息里面使用 `{}` 来作为占位符来输出变量，可直接使用 Scala 的字符串插值特性。scala-logging 基于 Scala macros 提供了编译时扩展：

```scala
logger.debug(s"Some $expensive message!")
```

将在编译时被替换为：

```scala
if (logger.isDebugEnabled) logger.debug(s"Some $expensive message!")
```

## 在 Akka 应用中使用

Akka 有自己的日志级别配置项。所以，最好将 Akka 的日志级别配置与 slf4j 的日志级别保持一致，可以在 HOCON 里面通过以下配置设置 Akka 的日志级别：

```hocon
akka.loglevel = "DEBUG"
```

在 actor 中，可以通过 `ActorContext[T]` 上提供的 `log` 方法来使用 Akka 日志。

当 `akka-actor-typed` 和 `akka-slf4j` 存在于类依赖路径上时，Akka 的事件日志处理 actor 将向 SLF4J 发送事件，并自动启用 `akka.event.slf4j.Slf4jLogger` 和 `akka.event.slf4j.Slf4jLoggingFilter` 类，而无需要任何配置。若需要手动配置 Akka 使用 SLF4J 输出日志，请确保如下配置，否则将使用默认日志实现并输出日志内容到终端。

```hocon
akka {
  loglevel = "DEBUG"
  loggers = ["akka.event.slf4j.Slf4jLogger"]
  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
}
```

@@@note { title=Tip }
使用 actor 的过程中，死信消息会在 **INFO** 级别输出，通常情况下这都是正常的业务状态，可以通过配置抑制这类日志消息在输出几次后被关闭的输出以免干扰我们正常的日志内容。另外，在 ActorSystem 被关闭（`terminate`）时，actor 邮箱里被挂起的消息将被发送的死信邮箱，我们可以通过配置禁止在 **terminate** 期间输出死信日志。

```hocon
akka {
  log-dead-letters = 10
  log-dead-letters-during-shutdown = on
}
```
@@@

## 在 Spring/ Spring Cloud 中使用

Spring 应用中需要删除`logback-spring.xml`文件而使用`logback.xml`（如果存在）。

Spring 应用需要禁用 `LoggingSystem`，使用此命令行参数可禁用它：`-Dorg.springframework.boot.logging.LoggingSystem=none`。[点此访问自定义 Spring 日志的更多介绍](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-custom-log-configuration)。

Fusion Log 为日志提供了兼容 Spring Cloud 的参数以在日志输出中输出 Spring应用服务名、启动环境（模式）、IP地址、网络端口等信息。详细映射参数见： @ref[#default-xml](#defaults-xml)


## 自定义 encoder

对于默认的 `STDOUT` 和 `FILE` 日志格式不满意的，可以自定义自己的日志编码格式。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="false">
    <include resource="fusion/log/logback/defaults.xml"/>
    <include resource="fusion/log/logback/stdout-appender.xml"/>

    <appender name="STDOUT_CUSTOM" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>-%d %-5level %fusionEnv %fusionServiceName %fusionServerHost %fusionServerPort [%thread] %logger{36} %line - %msg%n%exception</pattern>
        </encoder>
    </appender>

    <root level="DEBUG">
        <appender-ref ref="console_custom"/>
    </root>
</configuration>
```

## 预置配置

### defaults.xml

@@snip [defaults.xml](../../../../../fusion-log/src/main/resources/fusion/log/logback/defaults.xml)

Fusion Log 预定义了几个转换规则参数，可以在 Appender 的编码模式（encoder pattern）里通过 `%` 参数引用。这几个转换规则参数都将从Java应用Properties变量里查找所需的值。

1. `fusionServerHostName`：应用服务所在主机名，用以下方式读取：`InetAddress.getLocalHost.getCanonicalHostName`
0. `fusionServerIp`、`fusionServerHost`：应用服务绑定 IP 地址，查找顺序：
    - `-Dfusion.http.default.server.host`
    - `-Dhttp.host`
    - `-Dserver.host`
0. `fusionServerPort`：应用服务绑定网络端口，查找顺序：
    - `-Dfusion.http.default.server.port`
    - `-Dhttp.port`
    - `-Dserver.port`
0. `fusionServerName`：应用服务名字，查找顺序：
    - `-Dfusion.name`
    - `-Dspring.application.name`
    - `-Dfusion.service.name`
0. `fusionEnv`：应用服务启动环境（模式），如：`prod`、`test`、`dev`等运行环境，查找顺序：
    - `-Dfusion.profiles.active`
    - `-Dspring.profiles.active`
    - `-Drun.env`

### stdout-appender.xml

@@snip [defaults.xml](../../../../../fusion-log/src/main/resources/fusion/log/logback/stdout-appender.xml)

###　file-appender.xml

@@snip [defaults.xml](../../../../../fusion-log/src/main/resources/fusion/log/logback/file-appender.xml)

_通过 `LoggingEventCompositeJsonEncoder` 提供了 JSON 格式日志输出支持，它是 logstash 提供的一个 logback encoder 和 appender 库，在此可以查询更多详细：[https://github.com/logstash/logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) 。_

## 通过 Filebeat 输出日志到 Elastic-stack

### filebeat 配置

**filebeat.yml 参考配置如：**

@@snip [logback-file.xml](../../../../../fusion-log/example/filebeat.yml)

*注意是以下三行配置，使filebeat支持json格式日志文件*

```
  json:
    keys_under_root: true
    overwrite_keys: true
```

### 通过 Docker 启动 elastic-stack

这里选择使用 filebeat 直接把日志数据输出到 Elasticsearch，不使用 Logstack 做中转。

通过 docker-compose，可以很方便的启动 elastic-stack：`docker-compose up -d`

**docker-compose.yml**

@@snip [defaults.xml](../../../../../fusion-log/docker-compose.yml) 
