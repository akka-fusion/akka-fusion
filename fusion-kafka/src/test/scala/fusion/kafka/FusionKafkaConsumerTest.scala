/*
 * Copyright 2019 akka-fusion.com
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

import java.time.LocalTime
import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.Materializer
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import fusion.test.FusionTestFunSuite
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Await
import scala.concurrent.duration._

class FusionKafkaConsumerTest extends ScalaTestWithActorTestKit with FusionTestFunSuite {
  implicit private def ec = system.executionContext
  implicit private val classicSystem = system.toClassic
  implicit private val mat = Materializer(classicSystem)

  test("FusionKafkaConsumer") {
    val control = Consumer
      .plainSource(FusionKafkaConsumer(system).consumer, Subscriptions.topics("test"))
      .groupedWithin(100, 30.seconds)
      .map { records =>
        println(s"${LocalTime.now()} size ${records.size}")
        records.size
      }
      .toMat(Sink.fold(0L)((n, size) => n + size))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()

    TimeUnit.SECONDS.sleep(10)
    val producer = FusionKafkaProducer(system).producer.createKafkaProducer()
    (1 to 20).foreach { c =>
      producer.send(new ProducerRecord[String, String]("test", c.toString))
      TimeUnit.MILLISECONDS.sleep(50)
    }
    println(s"${LocalTime.now()} produce complete ")
    TimeUnit.SECONDS.sleep(5)
    println(s"${LocalTime.now()} begin drain shutdown")
    val f = control.drainAndShutdown()
    val result = Await.result(f, Duration.Inf)
    println(s"${LocalTime.now()} complete drain shutdown: $result")
    producer.close()
  }
}
