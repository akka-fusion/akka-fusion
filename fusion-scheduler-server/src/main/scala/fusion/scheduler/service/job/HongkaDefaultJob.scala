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

package fusion.scheduler.service.job

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import fusion.core.util.FusionUtils
import fusion.discovery.client.DiscoveryHttpClient
import fusion.http.util.HttpUtils
import fusion.job.ScheduleJob
import fusion.json.jackson.Jackson
import fusion.scheduler.constant.JobConstants
import helloscala.common.util.StringUtils
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.TriggerKey

import scala.jdk.CollectionConverters._
import scala.util.Failure
import scala.util.Success

case class JobData(jobKey: JobKey, triggerKey: TriggerKey, data: Map[String, String])

class HongkaDefaultJob extends ScheduleJob with StrictLogging {

  override def execute(context: JobExecutionContext): Unit = {
    performCallback(context)
  }

  private def performCallback(context: JobExecutionContext): Unit = {

    val dataMap: Map[String, String] = context.getMergedJobDataMap.asScala.map { case (k, v) => k -> v.toString }.toMap
    val callback = dataMap.getOrElse(JobConstants.CALLBACK, "")

    if (StringUtils.isNoneBlank(callback) && callback.startsWith("http")) {
      val system = FusionUtils.actorSystem()
      import system.executionContext

      val data = JobData(context.getJobDetail.getKey, context.getTrigger.getKey, dataMap)

      val request =
        HttpRequest(
          HttpMethods.POST,
          callback,
          entity = HttpEntity(ContentTypes.`application/json`, Jackson.stringify(data)))

      val responseF = if (FusionCore(system).configuration.discoveryEnable()) {
        DiscoveryHttpClient(system).request(request)
      } else {
        HttpUtils.singleRequest(request)(FusionCore(system).classicSystem)
      }

      responseF.onComplete {
        case Success(response) =>
          logger.debug(s"向远程服务发送回调错误完成，[${detailTrigger(context)}] callback地址：$callback。响应：$response")
        case Failure(e) =>
          logger.error(s"向远程服务发送回调错误，[${detailTrigger(context)}] callback地址：$callback", e)
      }
    }

  }

}
