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

import akka.actor.ActorSystem
import akka.kafka.ProducerMessage.PassThroughResult
import akka.kafka.ProducerMessage
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import fusion.json.jackson.Jackson
import fusion.test.FusionTestFunSuite
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._

case class FileEntity(_id: String, hash: String, suffix: String, localPath: String)

class KafkaTest extends FusionTestFunSuite with BeforeAndAfterAll {
  override def patienceTimeout: FiniteDuration = 10.seconds
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  val bootstrapServers = "192.168.31.98:9092"

  val topic = "uploaded-file"

  val producerSettings = FusionKafkaProducer(system).producer

  val consumerSettings = FusionKafkaConsumer(system).consumer

  test("producer") {
    val result = Source(List(FileEntity("jingyang", "hash", "suffix", "localPath")))
      .map(entity => new ProducerRecord[String, String](topic, Jackson.stringify(entity)))
      .runWith(Producer.plainSink(producerSettings))
      .futureValue
    println(result)
  }

  test("flexi") {
    Source(List(FileEntity("jingyang", "hash", "suffix", "localPath")))
      .map(entity => ProducerMessage.single(KafkaUtils.stringProduceRecord(topic, entity), PassThroughResult))
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

  override protected def afterAll(): Unit = {
    system.terminate()
  }
}
