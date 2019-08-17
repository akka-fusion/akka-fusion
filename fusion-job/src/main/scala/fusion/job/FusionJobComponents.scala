package fusion.job

import java.util

import akka.Done
import akka.actor.ExtendedActorSystem
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import fusion.core.util.Components
import fusion.jdbc.FusionJdbc
import fusion.job.impl.FactoryHelper
import fusion.job.impl.FusionJdbcConnectionProvider
import helloscala.common.Configuration
import org.quartz._
import org.quartz.impl.DefaultThreadExecutor
import org.quartz.impl.DirectSchedulerFactory
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.jdbcjobstore.JobStoreSupport
import org.quartz.simpl.RAMJobStore
import org.quartz.spi.JobStore
import org.quartz.spi.SchedulerPlugin
import org.quartz.spi.ThreadExecutor
import org.quartz.spi.ThreadPool
import org.quartz.utils.DBConnectionManager

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class FusionJobComponents(system: ExtendedActorSystem)
    extends Components[FusionScheduler]("fusion.job.default")
    with SchedulerFactory
    with StrictLogging {

  private val factory = DirectSchedulerFactory.getInstance()

  override def configuration: Configuration = FusionCore(system).configuration

  override protected def createComponent(id: String): FusionScheduler = {
    val c = configuration.getConfiguration(id).withFallback(configuration.getConfiguration("fusion.job._default_"))
    FusionScheduler(create(id, c), system)
  }

  override protected def componentClose(c: FusionScheduler): Future[Done] =
    Future {
      c.close()
      Done
    }(system.dispatcher)

  private def create(schedulerName: String, c: Configuration): Scheduler = {
    val helper              = new FactoryHelper(c.getProperties(""))
    val schedulerInstanceId = c.getString(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID)

    val threadPool             = createThreadPool(helper, c)
    val threadExecutor         = createThreadExecutor(helper, c)
    val jobStore               = createJobStore(helper, c)
    val schedulerPluginMap     = Map[String, SchedulerPlugin]()
    val rmiRegistryHost        = null
    val rmiRegistryPort        = 0
    val idleWaitTime           = helper.cfg.getLongProperty(StdSchedulerFactory.PROP_SCHED_IDLE_WAIT_TIME)
    val dbFailureRetryInterval = helper.cfg.getLongProperty(StdSchedulerFactory.PROP_SCHED_DB_FAILURE_RETRY_INTERVAL)
    val jmxExport              = helper.cfg.getBooleanProperty(StdSchedulerFactory.PROP_SCHED_JMX_EXPORT)
    val jmxObjectName          = c.getOrElse(StdSchedulerFactory.PROP_SCHED_JMX_OBJECT_NAME, "")
    val maxBatchSize           = 1
    val batchTimeWindow        = 0L
    factory.createScheduler(
      schedulerName,
      schedulerInstanceId,
      threadPool,
      threadExecutor,
      jobStore,
      schedulerPluginMap.asJava,
      rmiRegistryHost,
      rmiRegistryPort,
      idleWaitTime,
      dbFailureRetryInterval,
      jmxExport,
      jmxObjectName,
      maxBatchSize,
      batchTimeWindow)
    val scheduler       = getScheduler(schedulerName)
    val listenerManager = scheduler.getListenerManager
    configureListeners(helper, listenerManager)
    scheduler
  }

  private def configureListeners(helper: FactoryHelper, listenerManager: ListenerManager): Unit = {
    for (name <- helper.cfg.getPropertyGroups(StdSchedulerFactory.PROP_TRIGGER_LISTENER_PREFIX)) {
      val className = helper.cfg.getStringProperty(s"${StdSchedulerFactory.PROP_TRIGGER_LISTENER_PREFIX}.$name.class")
      system.dynamicAccess.createInstanceFor[TriggerListener](className, Nil) match {
        case Success(value) =>
          helper.setBeanProps(value, s"${StdSchedulerFactory.PROP_TRIGGER_LISTENER_PREFIX}.$name")
          listenerManager.addTriggerListener(value)
        case _ => // do nothing
      }
    }
    for (name <- helper.cfg.getPropertyGroups(StdSchedulerFactory.PROP_JOB_LISTENER_PREFIX)) {
      val className = helper.cfg.getStringProperty(s"${StdSchedulerFactory.PROP_JOB_LISTENER_PREFIX}.$name.class")
      system.dynamicAccess.createInstanceFor[JobListener](className, Nil) match {
        case Success(value) =>
          helper.setBeanProps(value, s"${StdSchedulerFactory.PROP_JOB_LISTENER_PREFIX}.$name")
          listenerManager.addJobListener(value)
        case _ => // do nothing
      }
    }
  }

  private def createJobStore(helper: FactoryHelper, c: Configuration): JobStore = {
    val className = helper.cfg.getStringProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS)
    val jobStore =
      system.dynamicAccess.createInstanceFor[JobStore](className, Nil) match {
        case Success(value) => value
        case Failure(e) =>
          logger.error(s"Create JobStore from $className error, use RAMJobStore", e)
          new RAMJobStore()
      }
    helper.setBeanProps(jobStore, StdSchedulerFactory.PROP_JOB_STORE_PREFIX)

    if (jobStore.isInstanceOf[JobStoreSupport]) {
      val fusionJdbcId = c.getString("org.quartz.jobStore.dataSource")
      val dataSource   = FusionJdbc(system).components.lookup(fusionJdbcId)
      val cp           = new FusionJdbcConnectionProvider(dataSource)
      DBConnectionManager.getInstance().addConnectionProvider(fusionJdbcId, cp)
    }

    jobStore
  }

  private def createThreadExecutor(helper: FactoryHelper, c: Configuration): ThreadExecutor = {
    Option(helper.cfg.getStringProperty(StdSchedulerFactory.PROP_THREAD_EXECUTOR_CLASS))
      .map { className =>
        system.dynamicAccess.createInstanceFor[ThreadExecutor](className, Nil) match {
          case Success(value) => helper.setBeanProps(value, StdSchedulerFactory.PROP_THREAD_EXECUTOR)
          case Failure(e) =>
            val msg = s"Create ThreadExecutor from $className error, using default implementation for ThreadExecutor"
            logger.info(msg, e)
            new DefaultThreadExecutor()
        }
      }
      .getOrElse {
        logger.info("Using default implementation for ThreadExecutor")
        new DefaultThreadExecutor()
      }
  }

  private def createThreadPool(helper: FactoryHelper, c: Configuration): ThreadPool = {
    val className = helper.cfg.getStringProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS)
    system.dynamicAccess.createInstanceFor[ThreadPool](className, Nil) match {
      case Success(value) => helper.setBeanProps(value, StdSchedulerFactory.PROP_THREAD_POOL_PREFIX)
      case Failure(e)     => throw e
    }
  }

  override def getScheduler(): Scheduler                      = factory.getScheduler
  override def getScheduler(schedName: String): Scheduler     = factory.getScheduler(schedName)
  override def getAllSchedulers(): util.Collection[Scheduler] = factory.getAllSchedulers
  def allSchedulers(): Vector[Scheduler]                      = factory.getAllSchedulers.asScala.toVector
}
