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
