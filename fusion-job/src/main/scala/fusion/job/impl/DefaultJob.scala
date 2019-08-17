package fusion.job.impl

import java.time.OffsetDateTime

import org.quartz.DisallowConcurrentExecution
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.UnableToInterruptJobException

class DefaultJob extends Job {
  override def execute(context: JobExecutionContext): Unit = {}
}

@DisallowConcurrentExecution
class DefaultDisallowConcurrentJob extends Job {
  override def execute(context: JobExecutionContext): Unit = {}
}

class DefaultInterruptableJob extends InterruptableJob {
  override def interrupt(): Unit = {
    throw new UnableToInterruptJobException(s"interrupt on ${OffsetDateTime.now()}")
  }

  override def execute(context: JobExecutionContext): Unit = {}
}
