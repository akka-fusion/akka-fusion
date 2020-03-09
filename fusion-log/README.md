# fusion-log

1. 通过 slf4j、logback 打印日志
2. 通过 logstash-logback-encoder 配置以 JSON 格式打印日志
    - 配置 filebeat 可以将日志直接输入到 ES（不需要通过 Logstash 中转）
