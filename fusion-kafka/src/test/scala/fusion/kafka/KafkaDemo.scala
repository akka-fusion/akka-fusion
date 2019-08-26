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

import akka.actor.ActorSystem
import akka.kafka.ProducerMessage
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

import scala.concurrent.Await
import scala.io.StdIn
import scala.concurrent.duration._

object KafkaDemo extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val (producerQueue, _) = Source
    .queue[String](128, OverflowStrategy.dropNew)
    .map(str => ProducerMessage.single(KafkaUtils.stringProduceRecord("test", str)))
    .via(Producer.flexiFlow(FusionKafkaProducer(system).producer))
    .toMat(Sink.foreach(result => println(result)))(Keep.both)
    .run()

  val (consumerControl, _) = Consumer
    .plainSource(FusionKafkaConsumer(system).consumer, Subscriptions.topics("test"))
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
