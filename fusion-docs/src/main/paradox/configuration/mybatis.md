# Fusion Mybatis 配置

@@snip [application.conf](../../../../../fusion-mybatis/src/test/resources/application.conf) { #mybatis }

`fusion-mybatis`依赖`fusion-jdbc`，需要通过`fusion-jdbc-source`配置来指定引用的`FusionJdbc`，配置如下：

@@snip [application.conf](../../../../../fusion-mybatis/src/test/resources/application.conf) { #jdbc }
