# Jdbc

Akka Fusion基于Akka Extension机制提供了配置化的 [HikariDataSource](https://github.com/brettwooldridge/HikariCP/blob/dev/src/main/java/com/zaxxer/hikari/HikariDataSource.java) 管理。 
同时，提供了 [JdbcTemplate](../../../../../fusion-jdbc/src/main/scala/fusion/jdbc/JdbcTemplate.scala) 来简化我们的JDBC编程工作。

## 示例

***配置 src/main/resources/application.conf***

@@snip [mysql.conf](../../../../../fusion-jdbc/src/test/resources/sample/mysql.conf) 

***测试代码***

@@snip [JdbcTemplateTest.scala](../../../../../fusion-jdbc/src/test/scala/fusion/jdbc/JdbcTemplateTest.scala)
