/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.job.impl.listener

import java.text.MessageFormat

import com.typesafe.scalalogging.StrictLogging
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

import scala.beans.BeanProperty

class LoggingJobListener extends JobListener with StrictLogging {
  @BeanProperty var jobToBeFiredMessage = "Job {1}.{0} fired (by trigger {4}.{3}) at: {2, date, yyyy-MM-dd HH:mm:ss}"

  @BeanProperty var jobSuccessMessage =
    "Job {1}.{0} execution complete at {2, date, yyyy-MM-dd HH:mm:ss} and reports: {8}"

  @BeanProperty var jobFailedMessage = "Job {1}.{0} execution failed at {2, date, yyyy-MM-dd HH:mm:ss} and reports: {8}"

  @BeanProperty var jobWasVetoedMessage =
    "Job {1}.{0} was vetoed.  It was to be fired (by trigger {4}.{3}) at: {2, date, yyyy-MM-dd HH:mm:ss}"

  override def getName: String = "LoggingJobListener"

  override def jobToBeExecuted(context: JobExecutionContext): Unit = logger.whenInfoEnabled {
    val trigger = context.getTrigger()
    logger.info(
      MessageFormat.format(
        getJobToBeFiredMessage,
        context.getJobDetail.getKey.getName,
        context.getJobDetail.getKey.getGroup,
        new java.util.Date(),
        trigger.getKey.getName,
        trigger.getKey.getGroup,
        trigger.getPreviousFireTime,
        trigger.getNextFireTime,
        Integer.valueOf(context.getRefireCount)))
  }

  override def jobExecutionVetoed(context: JobExecutionContext): Unit = logger.whenInfoEnabled {
    val trigger = context.getTrigger()
    logger.info(
      MessageFormat.format(
        getJobWasVetoedMessage,
        context.getJobDetail.getKey.getName,
        context.getJobDetail.getKey.getGroup,
        new java.util.Date(),
        trigger.getKey.getName,
        trigger.getKey.getGroup,
        trigger.getPreviousFireTime,
        trigger.getNextFireTime,
        Integer.valueOf(context.getRefireCount)))
  }

  override def jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException): Unit =
    logger.whenInfoEnabled {
      val trigger = context.getTrigger()
      if (jobException != null) {
        val errMsg = jobException.getMessage
        logger.warn(
          MessageFormat.format(
            getJobFailedMessage,
            context.getJobDetail.getKey.getName,
            context.getJobDetail.getKey.getGroup,
            new java.util.Date(),
            trigger.getKey.getName,
            trigger.getKey.getGroup,
            trigger.getPreviousFireTime,
            trigger.getNextFireTime,
            Integer.valueOf(context.getRefireCount),
            errMsg),
          jobException)
      } else {
        val result = String.valueOf(context.getResult)
        logger.info(
          MessageFormat.format(
            getJobSuccessMessage,
            context.getJobDetail.getKey.getName,
            context.getJobDetail.getKey.getGroup,
            new java.util.Date(),
            trigger.getKey.getName,
            trigger.getKey.getGroup,
            trigger.getPreviousFireTime,
            trigger.getNextFireTime,
            Integer.valueOf(context.getRefireCount),
            result))
      }

    }
}
