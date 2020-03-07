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

应用日志使用logback，由程序启动命令参数指定日志文件。如：`-Dlogback.configurationFile=${__APP_PATH__}/logback.xml`。

@@@warning { title=Spring }
Spring 应用中需要删除`logback-spring.xml`文件而使用`logback.xml`（如果存在）。

Spring 应用需要禁用 `LoggingSystem`，使用此命令行参数可禁用它：`-Dorg.springframework.boot.logging.LoggingSystem=none`。
@@@

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

## Filebeat 配置

**filebeat.yml 参考配置如：**

@@snip [logback-file.xml](../../../../../fusion-log/example/filebeat.yml)

*注意是以下三行配置，使filebeat支持json格式日志文件*

```
  json:
    keys_under_root: true
    overwrite_keys: true
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
