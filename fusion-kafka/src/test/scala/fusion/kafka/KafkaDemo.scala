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

package fusion.kafka

import akka.actor.typed.scaladsl.adapter._
import akka.kafka.{ProducerMessage, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.{actor => classic}
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.json.jackson.ScalaObjectMapper

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object KafkaDemo extends App {
  implicit val system = classic.ActorSystem()
  implicit val mat = Materializer.matFromSystem(system)
  import system.dispatcher
  private val objectMapper = new ScalaObjectMapper(new ObjectMapper())
  private val kafkaUtils = new KafkaUtils(objectMapper)

  val (producerQueue, _) = Source
    .queue[String](128, OverflowStrategy.dropNew)
    .map(str => ProducerMessage.single(kafkaUtils.stringProduceRecord("test", str)))
    .via(Producer.flexiFlow(FusionKafkaProducer(system.toTyped).producer))
    .toMat(Sink.foreach(result => println(result)))(Keep.both)
    .run()

  val (consumerControl, _) = Consumer
    .plainSource(FusionKafkaConsumer(system.toTyped).consumer, Subscriptions.topics("test"))
    .toMat(Sink.foreach(record => println(record)))(Keep.both)
    .run()

  Source(1 to 10)
    .map(_.toString)
    .throttle(1, 2.seconds)
    .runForeach(producerQueue.offer)
    .onComplete(tryValue => println(s"producer send over: $tryValue"))

  StdIn.readLine()
  producerQueue.complete()
//  consumerControl.shutdown()
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
