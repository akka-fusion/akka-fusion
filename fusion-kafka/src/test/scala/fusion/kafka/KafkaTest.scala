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

package fusion.kafka

import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ProducerMessage.PassThroughResult
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{ ProducerMessage, Subscriptions }
import akka.stream.Materializer
import akka.stream.scaladsl.{ Keep, Sink, Source }
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.json.jackson.ScalaObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.funsuite.AnyFunSuiteLike

case class FileEntity(_id: String, hash: String, suffix: String, localPath: String)

class KafkaTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike {
  implicit val classicSystem = system.toClassic
  implicit val mat = Materializer(classicSystem)
  val bootstrapServers = "192.168.31.98:9092"
  val topic = "uploaded-file"
  val producerSettings = FusionKafkaProducer(system).producer
  val consumerSettings = FusionKafkaConsumer(system).consumer
  private val objectMapper = new ScalaObjectMapper(new ObjectMapper())
  private val kafkaUtils = new KafkaUtils(objectMapper)

  test("producer") {
    val result = Source(List(FileEntity("jingyang", "hash", "suffix", "localPath")))
      .map(entity => new ProducerRecord[String, String](topic, objectMapper.stringify(entity)))
      .runWith(Producer.plainSink(producerSettings))
      .futureValue
    println(result)
  }

  test("flexi") {
    Source(List(FileEntity("jingyang", "hash", "suffix", "localPath")))
      .map(entity => ProducerMessage.single(kafkaUtils.stringProduceRecord(topic, entity), PassThroughResult))
      .via(Producer.flexiFlow(producerSettings))
  }

  test("consumer") {
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .map { record =>
        println(record.key() + ": " + record.value())
      }
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()

    TimeUnit.SECONDS.sleep(10)
  }
}
