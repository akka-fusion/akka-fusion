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

package fusion.docs.sample

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

import scala.concurrent.Await
import scala.io.StdIn
import scala.concurrent.duration._

object StreamDemo extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val (queue, resultF) = Source
    .queue[String](1024, OverflowStrategy.dropNew)
    .map(identity)
    .toMat(Sink.foreach(elem => println(s"queue elem: $elem")))(Keep.both)
    .run()

  resultF.onComplete(value => s"queue complete: $value")

  queue.offer("a") //.onComplete(println)
  TimeUnit.SECONDS.sleep(2)

  queue.offer("a") //.onComplete(println)
  TimeUnit.SECONDS.sleep(2)

  StdIn.readLine()
  queue.complete()
  Await.ready(queue.watchCompletion(), 10.seconds)

  system.terminate()
  Await.ready(system.whenTerminated, 10.seconds)

}
