package fusion.job.impl

import java.sql.Connection

import com.zaxxer.hikari.HikariDataSource
import org.quartz.utils.ConnectionProvider

class FusionJdbcConnectionProvider(dataSource: HikariDataSource) extends ConnectionProvider {
  override def getConnection: Connection = dataSource.getConnection

  override def shutdown(): Unit = {}

  override def initialize(): Unit = {}
}
