package fusion.kafka.getting

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

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
