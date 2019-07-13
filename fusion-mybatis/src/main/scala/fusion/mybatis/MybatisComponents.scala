package fusion.mybatis

import akka.actor.ExtendedActorSystem
import com.baomidou.mybatisplus.annotation.FieldStrategy
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder
import com.baomidou.mybatisplus.core.config.GlobalConfig
import com.baomidou.mybatisplus.core.config.GlobalConfig.DbConfig
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator
import com.baomidou.mybatisplus.extension.incrementer.DB2KeyGenerator
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator
import com.baomidou.mybatisplus.extension.incrementer.OracleKeyGenerator
import com.baomidou.mybatisplus.extension.incrementer.PostgreKeyGenerator
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.core.util.Components
import fusion.jdbc.FusionJdbc
import fusion.mybatis.constant.MybatisConstants
import helloscala.common.Configuration
import org.apache.ibatis.`type`.TypeHandler
import org.apache.ibatis.logging.Log
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory

import scala.util.Failure
import scala.util.Success

class MybatisComponents(system: ExtendedActorSystem)
    extends Components[FusionSqlSessionFactory](MybatisConstants.PATH_DEFAULT)
    with StrictLogging {
  def config: Config = system.settings.config

  override protected def createComponent(id: String): FusionSqlSessionFactory = {
    val c = Configuration(config.getConfig(id).withFallback(config.getConfig(MybatisConstants._PATH_DEFAULT)))

    val jdbcDataSourceId = c.getString(MybatisConstants.PATH_JDBC_NAME)
    val envId            = if (c.hasPath("env")) c.getString(s"env") else id
    val dataSource       = FusionJdbc(system).components.lookup(jdbcDataSourceId)
    val environment      = new Environment(envId, new JdbcTransactionFactory(), dataSource)

    val configuration = createConfiguration(c, environment)
    configuration.setGlobalConfig(createGlobalConfig(c))
    new FusionSqlSessionFactory(new MybatisSqlSessionFactoryBuilder().build(configuration))
  }

  private def createGlobalConfig(c: Configuration): GlobalConfig = {
    val gc = new GlobalConfig()

    val dbConfig = new DbConfig()
    c.computeIfForeach[String]("global-config.id-type", str => dbConfig.setIdType(IdType.valueOf(str)))
    c.computeIfForeach[String]("global-config.table-prefix", dbConfig.setTablePrefix)
    c.computeIfForeach[String]("global-config.schema", dbConfig.setSchema)
    c.computeIfForeach[String]("global-config.column-format", dbConfig.setColumnFormat)
    c.computeIfForeach[Boolean]("global-config.table-underline", dbConfig.setTableUnderline)
    c.computeIfForeach[Boolean]("global-config.capital-mode", dbConfig.setCapitalMode)
    c.computeIfForeach[String](
      "global-config.key-generator",
      keyGenerator => dbConfig.setKeyGenerator(getKeyGenerator(keyGenerator)))
    c.computeIfForeach[String]("global-config.logic-delete-value", dbConfig.setLogicDeleteValue)
    c.computeIfForeach[String]("global-config.logic-not-delete-value", dbConfig.setLogicNotDeleteValue)
    c.computeIfForeach[String](
      "global-config.field-strategy",
      fieldStrategy => dbConfig.setFieldStrategy(FieldStrategy.valueOf(fieldStrategy)))

    gc.setDbConfig(dbConfig)

    gc
  }

  private def getKeyGenerator(keyGenerator: String): IKeyGenerator = keyGenerator.toLowerCase match {
    case "postgres" | "postgre" => new PostgreKeyGenerator()
    case "db2"                  => new DB2KeyGenerator()
    case "h2"                   => new H2KeyGenerator()
    case "oracle"               => new OracleKeyGenerator()
    case other                  => throw new ExceptionInInitializerError(s"KeyGenerator 不存在：$other")
  }

  private def createConfiguration(c: Configuration, environment: Environment): MybatisConfiguration = {
    val configuration = new MybatisConfiguration(environment)

    val packagesPath = "configuration.package-names"
    val packageNames = c.getOrElse[Seq[String]](packagesPath, Nil)
    val mappersPath  = "configuration.mapper-names"
    val mapperNames  = c.getOrElse[Seq[String]](mappersPath, Nil)
    require(c.hasPath(packagesPath) || c.hasPath(mappersPath), s"$packagesPath 和 $mappersPath 配置不能同时为空")
    packageNames.foreach(packageName => configuration.addMappers(packageName))
    mapperNames.foreach(className => configuration.addMapper(Class.forName(className)))

    c.get[Option[String]]("configuration.default-enum-type-handler").foreach { className =>
      system.dynamicAccess.getClassFor[TypeHandler[_]](className) match {
        case Success(value)     => configuration.setDefaultEnumTypeHandler(value)
        case Failure(exception) => logger.error(s"$className 不是 ${classOf[Interceptor]}", exception)
      }
    }

    configuration.setMapUnderscoreToCamelCase(c.getOrElse[Boolean]("configuration.map-underscore-to-camel-case", true))

    configuration.setCacheEnabled(c.getOrElse[Boolean]("configuration.cache-enabled", false))

    configuration.setCallSettersOnNulls(c.getOrElse[Boolean]("configuration.call-setters-on-nulls", true))

    c.get[Option[String]]("configuration.log-impl").foreach { className =>
      system.dynamicAccess.getClassFor[Log](className) match {
        case Success(value)     => configuration.setLogImpl(value)
        case Failure(exception) => logger.error(s"$className 不是 ${classOf[Interceptor]}", exception)
      }
    }

    val interceptors = c.getOrElse[Seq[String]]("configuration.plugins", Nil) ++
          c.getOrElse[Seq[String]]("configuration.interceptors", Nil)
    interceptors.foreach { className =>
      system.dynamicAccess.createInstanceFor[Interceptor](className, Nil) match {
        case Success(value)     => configuration.addInterceptor(value)
        case Failure(exception) => logger.error(s"$className 不是 ${classOf[Interceptor]}", exception)
      }
    }

    configuration
  }

  override protected def componentClose(c: FusionSqlSessionFactory): Unit = {}
}
