/*
 * Copyright 2019-2021 helloscala.com
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

package docs.stream

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ ConsumerSettings, Subscriptions }
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.{ Done, actor => classic }
import fusion.json.jackson.JacksonObjectMapperExtension
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future
import scala.util.Random

object StreamExample {
  implicit val system = classic.ActorSystem()
  implicit val mat = Materializer.matFromSystem(system)
  import system.dispatcher

  def findOrgIdsByArea(area: String): Future[List[String]] =
    Future {
      (0 until Random.nextInt(50)).map(_.toString).toList
    }

  def findUserIdsByOrgId(orgId: String): Future[List[String]] =
    Future {
      (0 until Random.nextInt(50)).map(n => s"$orgId-$n").toList
    }

  def findImeisByUserIds(userIds: Iterable[String]): Future[List[String]] =
    Future {
      userIds.map(id => "imei-" + id).toList
    }

  def batchSendMessage(imeis: Seq[String], content: String): Unit = {
    println(s"发送消息内容为：$content, imeis个数：${imeis.size}")
  }

  def firstOnFuture(): Future[Unit] = {
    findOrgIdsByArea("北京")
      .flatMap { orgIds =>
        val futures = orgIds.map(id => findUserIdsByOrgId(id))
        Future.sequence(futures)
      }
      .flatMap { orgUserIdList =>
        val futures = orgUserIdList.map(userIds => findImeisByUserIds(userIds))
        Future.sequence(futures)
      }
      .map { orgImeiList =>
        orgImeiList.foreach(imeis => batchSendMessage(imeis, "推送消息"))
      }
  }

  def secondOnAkkaStream(): Future[Done] = {
    Source
      .future(findOrgIdsByArea("北京"))
      .mapConcat(identity)
      .mapAsync(4)(orgId => findUserIdsByOrgId(orgId))
      .mapAsync(4)(userIds => findImeisByUserIds(userIds))
      .mapConcat(identity)
      .grouped(1000)
      .runForeach(imeis => batchSendMessage(imeis, "推送消息"))
  }

  def secondOnAkkaStreamThrottle(): Future[Done] = {
    import scala.concurrent.duration._
    Source
      .future(findOrgIdsByArea("北京"))
      .mapConcat(identity)
      .mapAsync(4)(orgId => findUserIdsByOrgId(orgId))
      .mapAsync(4)(userIds => findImeisByUserIds(userIds))
      .mapConcat(identity)
      .grouped(1000)
      .throttle(5, 10.seconds)
      .runForeach(imeis => batchSendMessage(imeis, "推送消息"))
  }

  case class SendMessageByArea(area: String, content: String)

  def secondOnAkkaStreamKafka(): Future[Done] = {
    import scala.concurrent.duration._
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics("message"))
      .map(record =>
        JacksonObjectMapperExtension(system).objectMapperJson.convertValue[SendMessageByArea](record.value()))
      .flatMapConcat { req =>
        Source
          .future(findOrgIdsByArea(req.area))
          .mapConcat(identity)
          .mapAsync(4)(orgId => findUserIdsByOrgId(orgId))
          .mapAsync(4)(userIds => findImeisByUserIds(userIds))
          .mapConcat(identity)
          .grouped(1000)
          .throttle(5, 10.seconds)
          .map(imeis => batchSendMessage(imeis, req.content))
      }
      .runWith(Sink.ignore)
  }
}
