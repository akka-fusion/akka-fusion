# 起步

## 依赖

@@dependency[sbt,Maven,Gradle] {
  group="com.helloscala.fusion"
  artifact="fusion-mybatis_$scala.binary_version$"
  version="$version$"
}

## 示例

`FusionMyBatisTest` 演示了一个典型的 MyBatis 数据库操作。

@@snip [FusionMybatisTest.scala](../../../../../fusion-mybatis/src/test/scala/fusion/mybatis/FusionMybatisTest.scala) { #FusionMybatisTest }

`FileMapper`实现了 `BaseMapper[T]` 接口，它是MyBatis-plus对MyBatis提供的增强：

```scala
trait FileMapper extends BaseMapper[CFile] {
  def list(size: Int): java.util.List[CFile]
}
```

### `CFile`

@@snip [CFile.scala](../../../../../fusion-mybatis/src/test/scala/fusion/mybatis/model/CFile.scala) { #CFile }

### 配置与初始化

`application.conf`文件中

@@snip [application.conf](../../../../../fusion-mybatis/src/test/resources/application.conf)


