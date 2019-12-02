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

package fusion.schedulerx.protocol

import java.time.OffsetDateTime

import scala.concurrent.duration._

// Job实例数据
case class JobInstanceData(
    jobId: String,
    instanceId: String,
    name: String,
    `type`: JobType,
    schedulerTime: OffsetDateTime,
    jarUrl: Option[String] = None,
    mainClass: Option[String] = None,
    codeContent: Option[String] = None,
    timeout: FiniteDuration = 2.hours,
    startTime: Option[OffsetDateTime] = None,
    endTime: Option[OffsetDateTime] = None)
