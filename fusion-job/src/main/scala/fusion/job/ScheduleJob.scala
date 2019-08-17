package fusion.job

import org.quartz.Job
import org.quartz.JobExecutionContext

trait ScheduleJob extends Job {

  def detailTrigger(context: JobExecutionContext): String =
    context.getJobDetail.getKey.toString + ":" + context.getTrigger.getKey.toString
}
