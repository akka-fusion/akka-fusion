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

import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ProducerMessage
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.{ actor => classic }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object KafkaDemo extends App {
  implicit val system = classic.ActorSystem()
  implicit val mat = Materializer.matFromSystem(system)
  import system.dispatcher

  val (producerQueue, _) = Source
    .queue[String](128, OverflowStrategy.dropNew)
    .map(str => ProducerMessage.single(KafkaUtils.stringProduceRecord("test", str)))
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
    .runForeach(producerQueue.offer(_))
    .onComplete(tryValue => println(s"producer send over: $tryValue"))

  StdIn.readLine()
  producerQueue.complete()
//  consumerControl.shutdown()
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
