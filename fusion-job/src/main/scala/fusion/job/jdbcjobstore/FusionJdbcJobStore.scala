package fusion.job.jdbcjobstore

import java.sql.Connection

import org.quartz.impl.jdbcjobstore.JobStoreSupport
import org.quartz.impl.jdbcjobstore.JobStoreSupport.TransactionCallback

class FusionJdbcJobStore extends JobStoreSupport {
  override def getNonManagedTXConnection(): Connection = getConnection()

  override def executeInLock[T](lockName: String, txCallback: TransactionCallback[T]): T =
    executeInNonManagedTXLock(lockName, txCallback, null)
}
