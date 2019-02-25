package fusion.docs.sample

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}

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
