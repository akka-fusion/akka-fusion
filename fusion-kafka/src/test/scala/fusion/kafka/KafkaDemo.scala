package fusion.kafka

import akka.actor.ActorSystem
import akka.kafka.{ProducerMessage, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}

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
    .runForeach(producerQueue offer _)
    .onComplete(tryValue => println(s"producer send over: $tryValue"))

  StdIn.readLine()
  producerQueue.complete()
//  consumerControl.shutdown()
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
