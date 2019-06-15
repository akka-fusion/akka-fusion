package fusion.mybatis

import akka.actor.ActorSystem
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder
import com.typesafe.config.Config
import fusion.core.util.Components
import fusion.jdbc.FusionJdbc
import fusion.mybatis.constant.MybatisConstants
import helloscala.common.Configuration
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory

class MybatisComponents(system: ActorSystem)
    extends Components[FusionSqlSessionFactory](MybatisConstants.PATH_DEFAULT) {
  def config: Config = system.settings.config

  override protected def createComponent(id: String): FusionSqlSessionFactory = {
    val c                = Configuration(config)
    val jdbcDataSourceId = c.getString(s"$id.fusion-jdbc-source")
    val envId            = if (c.hasPath(s"$id.env")) c.getString(s"$id.env") else id
    val dataSource       = FusionJdbc(system).components.lookup(jdbcDataSourceId)
    val environment      = new Environment(envId, new JdbcTransactionFactory(), dataSource)
    val configuration    = new MybatisConfiguration(environment)

    val packagesPath = s"$id.package-names"
    val packageNames = c.getOrElse[Seq[String]](packagesPath, Nil)
    val mappersPath  = s"$id.mapper-names"
    val mapperNames  = c.getOrElse[Seq[String]](mappersPath, Nil)

    require(c.hasPath(packagesPath) || c.hasPath(mappersPath), s"$packagesPath 和 $mappersPath 配置不能同时为空")
    packageNames.foreach(packageName => configuration.addMappers(packageName))
    mapperNames.foreach(className => configuration.addMapper(Class.forName(className)))

//    val xmlConfigBuilder = new MybatisXMLConfigBuilder()

    new FusionSqlSessionFactory(new MybatisSqlSessionFactoryBuilder().build(configuration))
  }

  override protected def componentClose(c: FusionSqlSessionFactory): Unit = {}
}
