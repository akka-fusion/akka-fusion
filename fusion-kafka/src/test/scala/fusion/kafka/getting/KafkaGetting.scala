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

package fusion.kafka.getting

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Producer
import akka.kafka.ConsumerSettings
import akka.kafka.ProducerSettings
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object KafkaGetting extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher
  val config = system.settings.config

  val producerSettings =
    ProducerSettings(config.getConfig("akka.kafka.producer"), new StringSerializer, new StringSerializer)

  val consumerSettings =
    ConsumerSettings(config.getConfig("akka.kafka.consumer"), new StringDeserializer, new StringDeserializer)

  val producerQueue = Source
    .queue[String](128, OverflowStrategy.fail)
    .map(str => new ProducerRecord[String, String]("test", str))
    .toMat(Producer.plainSink(producerSettings))(Keep.left)
    .run()

  val consumerControl = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("test"))
    .map(record => record.value())
    .toMat(Sink.foreach(value => println(value)))(Keep.left)
    .run()

  Source(1 to 10)
    .map(_.toString)
    .throttle(1, 2.seconds)
    .runForeach(message => producerQueue.offer(message))
    .onComplete(tryValue => println(s"producer send over, return $tryValue"))

  println("Press 'enter' key exit.")
  StdIn.readLine()
  producerQueue.complete()
  consumerControl.shutdown()
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
