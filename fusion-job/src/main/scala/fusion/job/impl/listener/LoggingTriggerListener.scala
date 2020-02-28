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
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerListener

import scala.beans.BeanProperty

class LoggingTriggerListener extends TriggerListener with StrictLogging {
  @BeanProperty var triggerFiredMessage = "Trigger {1}.{0} fired job {6}.{5} at: {4, date, yyyy-MM-dd HH:mm:ss}"

  @BeanProperty var triggerMisfiredMessage =
    "Trigger {1}.{0} misfired job {6}.{5}  at: {4, date, yyyy-MM-dd HH:mm:ss}.  Should have fired at: {3, date, yyyy-MM-dd HH:mm:ss}"

  @BeanProperty var triggerCompleteMessage: String =
    "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, yyyy-MM-dd HH:mm:ss} with resulting trigger instruction code: {9}"

  override def getName: String = "LoggingTriggerListener"

  override def triggerFired(trigger: Trigger, context: JobExecutionContext): Unit = logger.whenInfoEnabled {
    logger.underlying.info(
      MessageFormat.format(
        getTriggerFiredMessage(),
        trigger.getKey.getName,
        trigger.getKey.getGroup,
        trigger.getPreviousFireTime,
        trigger.getNextFireTime,
        new java.util.Date(),
        context.getJobDetail.getKey.getName,
        context.getJobDetail.getKey.getGroup,
        Integer.valueOf(context.getRefireCount)))
  }

  override def vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean = false

  override def triggerMisfired(trigger: Trigger): Unit = logger.whenInfoEnabled {
    logger.underlying.info(
      MessageFormat.format(
        getTriggerCompleteMessage(),
        trigger.getKey.getName,
        trigger.getKey.getGroup,
        trigger.getPreviousFireTime,
        trigger.getNextFireTime,
        new java.util.Date,
        trigger.getJobKey.getName,
        trigger.getJobKey.getGroup))
  }

  override def triggerComplete(
      trigger: Trigger,
      context: JobExecutionContext,
      triggerInstructionCode: Trigger.CompletedExecutionInstruction): Unit = logger.whenInfoEnabled {
    val instrCode = triggerInstructionCode match {
      case CompletedExecutionInstruction.DELETE_TRIGGER                => "DELETE TRIGGER"
      case CompletedExecutionInstruction.NOOP                          => "DO NOTHING"
      case CompletedExecutionInstruction.RE_EXECUTE_JOB                => "RE-EXECUTE JOB"
      case CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE => "SET ALL OF JOB'S TRIGGERS COMPLETE"
      case CompletedExecutionInstruction.SET_TRIGGER_COMPLETE          => "SET THIS TRIGGER COMPLETE"
      case _                                                           => "UNKNOWN"
    }
    logger.underlying.info(
      MessageFormat.format(
        getTriggerCompleteMessage(),
        trigger.getKey.getName,
        trigger.getKey.getGroup,
        trigger.getPreviousFireTime,
        trigger.getNextFireTime,
        new java.util.Date,
        context.getJobDetail.getKey.getName,
        context.getJobDetail.getKey.getGroup,
        Integer.valueOf(context.getRefireCount),
        triggerInstructionCode.toString,
        instrCode))
  }
}
