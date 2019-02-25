package fusion.mybatis

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.jdbc.FusionJdbc
import fusion.mybatis.constant.MybatisConstants
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.{Configuration, SqlSessionFactory, SqlSessionFactoryBuilder}
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory

class MybatisComponents(system: ActorSystem) extends Components[SqlSessionFactory](MybatisConstants.PATH_DEFAULT) {
  def config: Config = system.settings.config

  override protected def createComponent(id: String): SqlSessionFactory = {
    val jdbcSourcePath = s"$id.jdbc-data-source"
    val configuration = new Configuration(
      new Environment(
        if (config.hasPath(s"$id.env")) config.getString(s"$id.env") else config.getString(jdbcSourcePath),
        new JdbcTransactionFactory(),
        FusionJdbc(system).components.lookup(config.getString(jdbcSourcePath))
      ))
    val packagesPath = s"$id.package-names"
    if (config.hasPath(packagesPath)) {
      config.getStringList(packagesPath).forEach(packageName => configuration.addMappers(packageName))
    }
    val mappersPath = s"$id.mapper-names"
    if (config.hasPath(mappersPath)) {
      config.getStringList(mappersPath).forEach(className => configuration.addMapper(Class.forName(className)))
    }
    require(config.hasPath(packagesPath) || config.hasPath(mappersPath), s"$packagesPath 和 $mappersPath 配置不能同时为空")
    new SqlSessionFactoryBuilder().build(configuration)
  }

  override protected def componentClose(c: SqlSessionFactory): Unit = {}
}

class FusionMybatis private (override protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components: MybatisComponents = new MybatisComponents(system)
  def component: SqlSessionFactory = components.component
}

object FusionMybatis extends ExtensionId[FusionMybatis] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMybatis = new FusionMybatis(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionMybatis
}
