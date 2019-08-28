# Fusion Docs

## Task todo list

#### fusion-http-gateway

1. 每个 upstream 可细粒度设置：
    - serviceName
    - targets
    - circuit-breaker
    - new-http-request: Boolean = 是否使用新的HttpRequest（这样将不代理原始的headers）
    - new-http-protocol: String = 1.1, 2.0
    - new-http-schema: String = http, https
    - not-proxy-headers: Array[String] = 

*upstream可继承location -> locations.proxy -> gateway.proxy 设置*
