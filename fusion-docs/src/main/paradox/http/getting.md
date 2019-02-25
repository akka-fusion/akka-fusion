# 开始

## 依赖

要使用 **Fusion Http**，需要在你的项目添加如下依赖：

@@dependency[sbt,Maven,Gradle] {
  group="com.helloscala.fusion"
  artifact="fusion-http_$scala.binary_version$"
  version="$version$"
}

## 示例程序

编写 `src/main/application.conf` 配置文件，添加以下配置：

```hocon
include "fusion-http.conf"
```

创建Scala文件：`src/main/scala/docs/http/SampleHttp.scala`

@@snip [SampleHttp](../../scala/docs/http/SampleHttp.scala) { #SampleHttp }

运行object `SampleHttp`，即可启动一个简单的Fusion HTTP应用。我们可以访问 `http://127.0.0.1:8000/hello` 来进行测试：

```
$ curl -i http://localhost:8000/hello
HTTP/1.1 200 OK
Fusion-Server: default/0:0:0:0:0:0:0:0:8000
Server: akka-http/10.1.7
Date: Thu, 21 Feb 2019 09:42:02 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 22

Hello，Akka Fusion！
```

## 内置API

同时，Fusion HTTP还提供了健库检测和管理功能接口。

1. **健康检测**
    
    ```
    $ curl -i http://127.0.0.1:8558/_management/health/alive
    HTTP/1.1 200 OK
    Content-Length: 2
    Content-Type: text/plain; charset=UTF-8
    Date: Thu, 21 Feb 2019 09:52:05 GMT
    Keep-Alive: timeout=38
    Server: akka-http/10.1.7
    
    OK
    ```

2. **关闭应用** 
    
    ```
    $ curl -i -XPOST http://127.0.0.1:8558/_management/fusion/shutdown
    HTTP/1.1 200 OK
    Content-Length: 62
    Content-Type: application/json
    Date: Thu, 21 Feb 2019 09:52:57 GMT
    Keep-Alive: timeout=38
    Server: akka-http/10.1.7
    
    {"status":200,"message":"1 second后开始关闭Fusion系统"}
    ```
